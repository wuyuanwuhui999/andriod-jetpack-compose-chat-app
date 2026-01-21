// Avatar.kt
package com.player.chat.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import coil.compose.AsyncImage
import com.player.chat.config.Config
import com.player.chat.R
import com.player.chat.ui.theme.Dimens

enum class AvatarSize {
    BIG, MIDDLE, SMALL
}

@Composable
fun Avatar(
    avatarUrl: String?,
    size: AvatarSize,
    modifier: Modifier = Modifier
) {
    val sizeDp: Dp = when (size) {
        AvatarSize.BIG -> Dimens.bigAvater
        AvatarSize.MIDDLE -> Dimens.middleAvater
        AvatarSize.SMALL -> Dimens.smallAvater
    }

    val finalUrl = if (!avatarUrl.isNullOrBlank()) {
        Config.BASE_URL + avatarUrl
    } else null

    if (finalUrl != null) {
        AsyncImage(
            model = finalUrl,
            contentDescription = "用户头像",
            modifier = modifier
                .size(sizeDp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop,
            placeholder = painterResource(R.drawable.icon_user),
            error = painterResource(R.drawable.icon_user)
        )
    } else {
        Image(
            painter = painterResource(R.drawable.icon_user),
            contentDescription = "默认头像",
            modifier = modifier
                .size(sizeDp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
    }
}