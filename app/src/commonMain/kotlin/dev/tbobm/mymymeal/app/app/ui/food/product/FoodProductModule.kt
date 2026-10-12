package dev.tbobm.mymymeal.app.app.ui.food.product

import dev.tbobm.mymymeal.app.app.ui.food.product.create.CreateProductViewModel
import dev.tbobm.mymymeal.app.app.ui.food.product.download.DownloadProductHolder
import dev.tbobm.mymymeal.app.app.ui.food.product.download.DownloadProductViewModel
import dev.tbobm.mymymeal.app.app.ui.food.product.update.UpdateProductViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf

fun Module.foodProduct() {
    viewModelOf(::CreateProductViewModel)
    viewModelOf(::UpdateProductViewModel)
    viewModel { (text: String?, holder: DownloadProductHolder) ->
        DownloadProductViewModel(text, get(), holder)
    }
    viewModelOf(::DownloadProductHolder)
}
