package dev.tbobm.mymymeal.app.app.ui.common.component

import androidx.compose.runtime.*
import dev.tbobm.mymymeal.app.barcodescanner.ui.CameraBarcodeScannerScreen
import dev.tbobm.mymymeal.app.common.compose.component.FullScreenDialog

@Composable
fun FullScreenCameraBarcodeScanner(onBarcodeScan: (String) -> Unit, onClose: () -> Unit) {
    // TODO
    //  Predictive back handling
    FullScreenDialog(onDismissRequest = onClose) {
        CameraBarcodeScannerScreen(onBarcodeScan = onBarcodeScan, onClose = onClose)
    }
}

@Composable
fun FullScreenCameraBarcodeScanner(
    visible: Boolean,
    onBarcodeScan: (String) -> Unit,
    onClose: () -> Unit,
) {
    if (visible) {
        FullScreenCameraBarcodeScanner(onBarcodeScan, onClose)
    }
}
