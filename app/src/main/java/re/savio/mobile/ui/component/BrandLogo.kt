package re.savio.mobile.ui.component



import androidx.compose.foundation.layout.Box

import androidx.compose.foundation.layout.fillMaxSize

import androidx.compose.runtime.Composable

import androidx.compose.runtime.remember

import androidx.compose.ui.Alignment

import androidx.compose.ui.Modifier

import androidx.compose.ui.layout.ContentScale

import androidx.compose.ui.platform.LocalContext

import coil3.compose.SubcomposeAsyncImage

import coil3.compose.SubcomposeAsyncImageContent

import coil3.network.NetworkHeaders

import coil3.network.httpHeaders

import coil3.request.ImageRequest

import coil3.request.crossfade

import re.savio.mobile.BuildConfig



@Composable

fun BrandLogo(

    brandId: String?,

    modifier: Modifier = Modifier,

    fallback: @Composable () -> Unit = {},

) {

    if (brandId.isNullOrBlank()) {

        fallback()

        return

    }



    val context = LocalContext.current

    val banBaseUrl = BuildConfig.BAN_API_BASE_URL.trimEnd('/')

    val banApiKey = BuildConfig.BAN_API_KEY

    val url = "$banBaseUrl/nomenclature/$brandId/icon"



    val headers = remember(banApiKey) {

        NetworkHeaders.Builder()

            .set("x-api-key", banApiKey)

            .build()

    }



    val imageRequest = remember(url, headers) {

        ImageRequest.Builder(context)

            .data(url)

            .httpHeaders(headers)

            .crossfade(300)

            .build()

    }



    Box(

        modifier = modifier,

        contentAlignment = Alignment.Center,

    ) {

        SubcomposeAsyncImage(

            model = imageRequest,

            contentDescription = null,

            modifier = Modifier.fillMaxSize(),

            contentScale = ContentScale.Inside,

            loading = { fallback() },

            error = { fallback() },

            success = { SubcomposeAsyncImageContent() },

        )

    }

}

