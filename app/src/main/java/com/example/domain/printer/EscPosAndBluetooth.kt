package com.example.domain.printer

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.nio.charset.Charset
import java.util.UUID

class EscPosBuilder {
    private val buffer = ByteArrayOutputStream()

    fun initialize(): EscPosBuilder {
        buffer.write(byteArrayOf(0x1B, 0x40)) // ESC @
        return this
    }

    fun alignLeft(): EscPosBuilder {
        buffer.write(byteArrayOf(0x1B, 0x61, 0x00))
        return this
    }

    fun alignCenter(): EscPosBuilder {
        buffer.write(byteArrayOf(0x1B, 0x61, 0x01))
        return this
    }

    fun alignRight(): EscPosBuilder {
        buffer.write(byteArrayOf(0x1B, 0x61, 0x02))
        return this
    }

    fun bold(enable: Boolean): EscPosBuilder {
        buffer.write(byteArrayOf(0x1B, 0x45, if (enable) 0x01 else 0x00))
        return this
    }

    fun text(text: String): EscPosBuilder {
        buffer.write(text.toByteArray(Charset.forName("CP437")))
        return this
    }

    fun line(text: String): EscPosBuilder {
        text(text)
        buffer.write(0x0A) // LF
        return this
    }

    fun feed(lines: Int = 3): EscPosBuilder {
        buffer.write(byteArrayOf(0x1B, 0x64, lines.toByte()))
        return this
    }

    fun cutPaper(): EscPosBuilder {
        buffer.write(byteArrayOf(0x1D, 0x56, 0x41, 0x00)) // GS V A 0
        return this
    }

    fun openCashDrawer(): EscPosBuilder {
        buffer.write(byteArrayOf(0x1B, 0x70, 0x00, 0x19, 0xFA.toByte()))
        return this
    }

    fun build(): ByteArray = buffer.toByteArray()
}

class BluetoothPrinterManager(
    private val context: Context
) {
    private val sppUuid: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    @SuppressLint("MissingPermission")
    fun getPairedPrinters(): List<BluetoothDevice> {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
        val adapter = bluetoothManager?.adapter ?: BluetoothAdapter.getDefaultAdapter() ?: return emptyList()
        return try {
            adapter.bondedDevices?.toList() ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    @SuppressLint("MissingPermission")
    suspend fun printReceipt(deviceAddress: String, data: ByteArray): Result<Unit> = withContext(Dispatchers.IO) {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
        val adapter = bluetoothManager?.adapter ?: BluetoothAdapter.getDefaultAdapter()
            ?: return@withContext Result.failure(IllegalStateException("Bluetooth not available on this device"))

        if (!adapter.isEnabled) {
            return@withContext Result.failure(IllegalStateException("Bluetooth is turned off"))
        }

        var socket: BluetoothSocket? = null
        var outputStream: OutputStream? = null
        try {
            val device = adapter.getRemoteDevice(deviceAddress)
            adapter.cancelDiscovery()

            socket = device.createRfcommSocketToServiceRecord(sppUuid)
            socket.connect()

            outputStream = socket.outputStream
            outputStream.write(data)
            outputStream.flush()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            try { outputStream?.close() } catch (_: Exception) {}
            try { socket?.close() } catch (_: Exception) {}
        }
    }
}
