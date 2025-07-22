package tirumalesh.app.qrcodegeneratorapp.qrcodedatabase

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase


@Database(entities = [QrCodeEntity::class], version = 1)
abstract class QrCodeDatabase : RoomDatabase() {
    abstract fun qrCodeDao(): QrCodeDao

    companion object {
        @Volatile
        private var INSTANCE: QrCodeDatabase? = null

        fun getDatabase(context: Context): QrCodeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    QrCodeDatabase::class.java,
                    "qr_code_db"
                )
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}