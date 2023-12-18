package com.example.inventory.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.inventory.ui.setting.KeyManager.dbKey
import com.example.inventory.ui.setting.SQLCipherUtils
import net.sqlcipher.database.SupportFactory
import java.io.IOException

private const val DB_NAME = "item_database_lab4"
private val PASSPHRASE = dbKey
    /*
@Database(entities = [Item::class], version = 4, exportSchema = false)
abstract class InventoryDatabase : RoomDatabase() {
    abstract fun itemDao(): ItemDao

    companion object {
        //@Volatile
        //private var Instance: InventoryDatabase? = null

        fun getDatabase(context: Context): InventoryDatabase {
            synchronized(this) {
                val dbFile = context.getDatabasePath(DB_NAME)
                val passphrase = PASSPHRASE.toByteArray()
                val state = SQLCipherUtils.getDatabaseState(context, dbFile)

                if (state == SQLCipherUtils.State.UNENCRYPTED) {
                    val dbTemp = context.getDatabasePath("_temp.db")
                    dbTemp.delete()

                    SQLCipherUtils.encryptTo(context, dbFile, dbTemp, passphrase)
                    val dbBackup = context.getDatabasePath("_backup.db")

                    if (dbFile.renameTo(dbBackup)) {
                        if (dbTemp.renameTo(dbFile)) {
                            dbBackup.delete()
                        } else {
                            dbBackup.renameTo(dbFile)
                            throw IOException("Could not rename $dbTemp to $dbFile")
                        }
                    } else {
                        dbTemp.delete()
                        throw IOException("Could not rename $dbFile to $dbBackup")
                    }
                }

                return Room.databaseBuilder(context, InventoryDatabase::class.java, DB_NAME)
                    .openHelperFactory(SupportFactory(passphrase))
                    .build()

            }
        }

    }
}


     */


@Database(entities = [Item::class], version = 2, exportSchema = false)
abstract class InventoryDatabase : RoomDatabase() {
    abstract fun itemDao(): ItemDao
    companion object {
        @Volatile
        private var Instance: InventoryDatabase? = null
        fun getDatabase(context: Context): InventoryDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, InventoryDatabase::class.java, "item_database_lab4")
                    .fallbackToDestructiveMigration().build().also { Instance = it }
            }

        }
    }

}

