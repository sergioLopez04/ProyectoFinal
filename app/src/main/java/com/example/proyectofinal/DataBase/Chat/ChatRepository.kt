package com.example.proyectofinal.DataBase.Chat

import android.util.Log
import com.example.proyectofinal.Modelos.Mensaje
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ChatRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private fun messagesRef(projectId: String) =
        db.collection("projects")
            .document(projectId)
            .collection("messages")

    fun getMessages(projectId: String): Flow<List<Mensaje>> = callbackFlow {
        val sub = messagesRef(projectId)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snap, e ->
                if (e != null) return@addSnapshotListener
                val list = snap!!.documents.map { doc ->
                    val data = doc.data ?: emptyMap<String, Any>()
                    // Firestore Timestamp
                    val ts = data["timestamp"] as? com.google.firebase.Timestamp
                    Mensaje(
                        id = doc.id,
                        authorId = data["authorId"] as String,
                        text = data["text"] as String,
                        timestamp = ts?.toDate()?.time ?: 0L
                    )
                }
                trySend(list)
            }
        awaitClose { sub.remove() }
    }

    suspend fun getUserNames(uids: List<String>): Map<String, String> {
        val usersCollection = db.collection("users") // o como sea tu colección
        val result = mutableMapOf<String, String>()

        uids.distinct().chunked(10).forEach { chunk -> // evita exceder límites de Firestore
            val query = usersCollection.whereIn("uid", chunk)
            val snapshot = query.get().await()
            snapshot.documents.forEach { doc ->
                val uid = doc.getString("uid") ?: return@forEach
                val name = doc.getString("name") ?: "Desconocido"
                result[uid] = name
            }
        }

        return result
    }



    suspend fun sendMessage(projectId: String, text: String) {
        val user = auth.currentUser ?: return
        val data = mapOf(
            "authorId" to user.uid,
            "text" to text,
            "timestamp" to FieldValue.serverTimestamp()
        )
        messagesRef(projectId).add(data).await()
    }

    fun getMembers(projectId: String): Flow<List<String>> = callbackFlow {
        val ref = db.collection("projects").document(projectId)
        val sub = ref.addSnapshotListener { snap, _ ->
            val list = (snap?.get("members") as? List<String>) ?: emptyList()
            trySend(list)
        }
        awaitClose { sub.remove() }
    }


}