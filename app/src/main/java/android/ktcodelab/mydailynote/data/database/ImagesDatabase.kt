package android.ktcodelab.mydailynote.data.database

import android.ktcodelab.mydailynote.data.database.entity.ImageToDelete
import android.ktcodelab.mydailynote.data.database.entity.ImageToUpload
import androidx.room.Database
import androidx.room.RoomDatabase


@Database(

    entities = [ImageToUpload::class, ImageToDelete::class],
    version = 2,
    exportSchema = false
)
abstract class ImagesDatabase(): RoomDatabase() {

    abstract fun imageToUploadDao(): ImageToUploadDao
    abstract fun imageToDeleteDao(): ImageToDeleteDao

}
