package dev.tbobm.mymymeal.app.common.infrastructure.crypto

import dev.tbobm.mymymeal.app.common.crypto.IdentityCrypto
import dev.tbobm.mymymeal.app.common.crypto.MasterCrypto
import dev.tbobm.mymymeal.app.common.crypto.SignatureVerifier
import org.koin.core.definition.KoinDefinition
import org.koin.core.module.Module

internal expect fun Module.masterCryptoDefinition(): KoinDefinition<out MasterCrypto>

internal expect fun Module.identityCryptoDefinition(): KoinDefinition<out IdentityCrypto>

internal expect fun Module.signatureVerifierDefinition(): KoinDefinition<out SignatureVerifier>

fun Module.cryptoModule() {
    masterCryptoDefinition()
    identityCryptoDefinition()
    signatureVerifierDefinition()
}
