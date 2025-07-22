package tirumalesh.app.qrcodegeneratorapp.qrcodedatabase

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query


@Dao
interface QrCodeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQrCode(qrCode: QrCodeEntity)

    @Query("SELECT * FROM qr_codes")
    suspend fun getAllQrCodes(): List<QrCodeEntity>

    @Delete
    suspend fun deleteQrCode(qrCode: QrCodeEntity)
}