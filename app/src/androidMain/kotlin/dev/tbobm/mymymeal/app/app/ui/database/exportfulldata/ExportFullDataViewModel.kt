package dev.tbobm.mymymeal.app.app.ui.database.exportfulldata

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.tbobm.mymymeal.app.importexport.domain.entity.ProductField
import dev.tbobm.mymymeal.app.importexport.domain.usecase.ExportCsvProductsUseCase
import dev.tbobm.mymymeal.app.importexport.domain.usecase.ExportDiaryEntriesUseCase
import dev.tbobm.mymymeal.app.importexport.domain.usecase.ExportRecipeIngredientsUseCase
import dev.tbobm.mymymeal.app.importexport.domain.usecase.ExportRecipesUseCase
import java.io.BufferedWriter
import java.io.OutputStreamWriter
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal class ExportFullDataViewModel(
    private val exportCsvProductsUseCase: ExportCsvProductsUseCase,
    private val exportDiaryEntriesUseCase: ExportDiaryEntriesUseCase,
    private val exportRecipesUseCase: ExportRecipesUseCase,
    private val exportRecipeIngredientsUseCase: ExportRecipeIngredientsUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow<UiState>(UiState.WaitingForFile)
    val uiState = _uiState.asStateFlow()

    fun handleZip(uri: Uri, context: Context) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val stream = context.contentResolver.openOutputStream(uri)

                    if (stream == null) {
                        _uiState.value =
                            UiState.Error(
                                "Failed to open file. Please ensure the file exists and is accessible."
                            )
                        return@withContext
                    }

                    addCloseable(stream)

                    var count = 0
                    ZipOutputStream(stream).use { zip ->
                        count += writeEntry(zip, "entries.csv", exportDiaryEntriesUseCase.export())
                        count +=
                            writeEntry(
                                zip,
                                "foods.csv",
                                exportCsvProductsUseCase.export(ProductField.entries),
                            )
                        count += writeEntry(zip, "recipes.csv", exportRecipesUseCase.export())
                        count +=
                            writeEntry(
                                zip,
                                "recipe-ingredients.csv",
                                exportRecipeIngredientsUseCase.export(),
                            )
                    }

                    _uiState.value = UiState.Exported(count)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    _uiState.value = UiState.Error(e.message)
                }
            }
        }
    }

    /** Writes [lines] as a single zip entry named [name]. Returns the number of lines written. */
    private suspend fun writeEntry(zip: ZipOutputStream, name: String, lines: Flow<String>): Int {
        zip.putNextEntry(ZipEntry(name))
        val writer = BufferedWriter(OutputStreamWriter(zip))

        var count = 0
        lines.collect { line ->
            writer.appendLine(line)
            count++
            _uiState.value = UiState.Exporting(count)
        }

        writer.flush()
        zip.closeEntry()
        return count
    }
}
