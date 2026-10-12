package dev.tbobm.mymymeal.app.food.domain.usecase

import dev.tbobm.mymymeal.app.common.log.Logger
import dev.tbobm.mymymeal.app.common.log.logAndReturnFailure
import dev.tbobm.mymymeal.app.common.result.Ok
import dev.tbobm.mymymeal.app.common.result.Result
import dev.tbobm.mymymeal.app.common.result.fold
import dev.tbobm.mymymeal.app.food.domain.entity.RemoteFoodException
import dev.tbobm.mymymeal.app.food.domain.entity.RemoteProduct
import dev.tbobm.mymymeal.app.food.domain.repository.RemoteProductRequestFactory

sealed interface DownloadProductError {

    data object UrlNotFound : DownloadProductError

    data object UrlNotSupported : DownloadProductError

    data object InvalidAiResponse : DownloadProductError

    data class RemoteFoodError(val exception: RemoteFoodException) : DownloadProductError
}

class DownloadProductUseCase(
    private val remoteRequestFactory: RemoteProductRequestFactory,
    private val logger: Logger,
) {
    suspend fun download(url: String): Result<RemoteProduct, DownloadProductError> {
        if ('{' in url) {
            val product = parseAiProduct(url)
            return if (product != null) {
                Ok(product)
            } else {
                logger.logAndReturnFailure(
                    tag = TAG,
                    error = DownloadProductError.InvalidAiResponse,
                    message = { "Could not parse AI response" },
                )
            }
        }

        val link = linkRegex.find(url)?.value

        if (link == null) {
            return logger.logAndReturnFailure(
                tag = TAG,
                error = DownloadProductError.UrlNotFound,
                message = { "No valid URL found in: $url" },
            )
        }

        val request = remoteRequestFactory.create(link)

        if (request == null) {
            return logger.logAndReturnFailure(
                tag = TAG,
                error = DownloadProductError.UrlNotSupported,
                message = { "No supported request found for URL: $link" },
            )
        }

        return request
            .execute()
            .fold(
                onSuccess = ::Ok,
                onError = { error ->
                    logger.logAndReturnFailure(
                        tag = TAG,
                        throwable = error,
                        error = DownloadProductError.RemoteFoodError(error),
                        message = { "Error when fetching product for URL: $link" },
                    )
                },
            )
    }

    private val linkRegex by lazy {
        Regex(
            """(?:https://)?[-a-zA-Z0-9@:%._+~#=]{1,256}\.[a-zA-Z0-9()]{1,6}\b([-a-zA-Z0-9()@:%_+.~#?&/=]*)""",
            RegexOption.IGNORE_CASE,
        )
    }

    private companion object {
        private const val TAG = "DownloadProductUseCaseImpl"
    }
}
