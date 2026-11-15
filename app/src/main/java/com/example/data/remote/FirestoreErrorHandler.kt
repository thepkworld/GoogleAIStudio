package com.example.data.remote

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestoreException

enum class OperationType {
    CREATE,
    UPDATE,
    DELETE,
    LIST,
    GET,
    WRITE
}

data class FirestoreErrorInfo(
    val error: String,
    val operationType: OperationType,
    val path: String?,
    val userId: String?
)

fun handleFirestoreError(
    error: Throwable,
    operationType: OperationType,
    path: String?,
    auth: FirebaseAuth
): Nothing {
    val errInfo = FirestoreErrorInfo(
        error = error.message ?: "Unknown Firestore error",
        operationType = operationType,
        path = path,
        userId = auth.currentUser?.uid
    )
    val errMessage =
        "Firestore Error: operation=${errInfo.operationType}, path=${errInfo.path}, userId=${errInfo.userId}, msg=${errInfo.error}"
    Log.e("FirestoreError", errMessage, error)
    throw FirebaseFirestoreException(
        errMessage,
        (error as? FirebaseFirestoreException)?.code ?: FirebaseFirestoreException.Code.UNKNOWN,
        error
    )
}
