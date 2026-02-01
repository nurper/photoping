package com.photoping.data.photo.upload

import com.google.firebase.storage.StorageMetadata
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID

class FirebaseStorageImageUploader : ImageUploader {
    override suspend fun uploadJpeg(bytes: ByteArray, pathHint: String): String {
        val safeHint = pathHint.trim().ifBlank { "unknown" }
            .replace("/", "_")
            .replace("\\", "_")

        val objectName = "${UUID.randomUUID()}.jpg"
        val ref = FirebaseStorage.getInstance().reference
            .child("photopings")
            .child(safeHint)
            .child(objectName)

        val metadata = StorageMetadata.Builder()
            .setContentType("image/jpeg")
            .build()

        ref.putBytes(bytes, metadata).await()
        return ref.downloadUrl.await().toString()
    }
}
