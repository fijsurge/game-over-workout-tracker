package com.gameovertracker.wear.presentation

import android.util.Log
import androidx.concurrent.futures.ResolvableFuture
import androidx.wear.protolayout.ActionBuilders
import androidx.wear.protolayout.ColorBuilders
import androidx.wear.protolayout.DimensionBuilders
import androidx.wear.protolayout.LayoutElementBuilders
import androidx.wear.protolayout.ModifiersBuilders
import androidx.wear.tiles.ResourceBuilders
import androidx.wear.protolayout.TimelineBuilders
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.TileBuilders
import androidx.wear.tiles.TileService
import com.google.common.util.concurrent.ListenableFuture

class WorkoutTileService : TileService() {

    override fun onTileRequest(requestParams: RequestBuilders.TileRequest): ListenableFuture<TileBuilders.Tile> {
        Log.d("WorkoutTile", "onTileRequest called")
        return try {
            val future = ResolvableFuture.create<TileBuilders.Tile>()
            future.set(buildTile())
            Log.d("WorkoutTile", "tile built and future resolved")
            future
        } catch (e: Exception) {
            Log.e("WorkoutTile", "Exception in buildTile", e)
            ResolvableFuture.create<TileBuilders.Tile>().also { it.setException(e) }
        }
    }

    @Suppress("RETURN_TYPE_MISMATCH_ON_OVERRIDE")
    override fun onResourcesRequest(requestParams: RequestBuilders.ResourcesRequest): ListenableFuture<ResourceBuilders.Resources> {
        val future = ResolvableFuture.create<ResourceBuilders.Resources>()
        future.set(ResourceBuilders.Resources.Builder().setVersion("1").build())
        return future
    }

    private fun buildTile(): TileBuilders.Tile {
        val launchAction = ActionBuilders.LaunchAction.Builder()
            .setAndroidActivity(
                ActionBuilders.AndroidActivity.Builder()
                    .setPackageName(packageName)
                    .setClassName("com.gameovertracker.wear.MainActivity")
                    .build()
            ).build()

        val clickable = ModifiersBuilders.Clickable.Builder()
            .setOnClick(launchAction).build()

        val layout = LayoutElementBuilders.Box.Builder()
            .setWidth(DimensionBuilders.expand())
            .setHeight(DimensionBuilders.expand())
            .setHorizontalAlignment(LayoutElementBuilders.HORIZONTAL_ALIGN_CENTER)
            .setVerticalAlignment(LayoutElementBuilders.VERTICAL_ALIGN_CENTER)
            .setModifiers(ModifiersBuilders.Modifiers.Builder().setClickable(clickable).build())
            .addContent(
                LayoutElementBuilders.Column.Builder()
                    .setHorizontalAlignment(LayoutElementBuilders.HORIZONTAL_ALIGN_CENTER)
                    .setWidth(DimensionBuilders.wrap())
                    .setHeight(DimensionBuilders.wrap())
                    .addContent(
                        LayoutElementBuilders.Text.Builder()
                            .setText("GAME OVER")
                            .setFontStyle(
                                LayoutElementBuilders.FontStyle.Builder()
                                    .setColor(ColorBuilders.argb(0xFFDD0000.toInt()))
                                    .setSize(DimensionBuilders.sp(16f))
                                    .build()
                            ).build()
                    )
                    .addContent(
                        LayoutElementBuilders.Spacer.Builder()
                            .setHeight(DimensionBuilders.dp(4f)).build()
                    )
                    .addContent(
                        LayoutElementBuilders.Text.Builder()
                            .setText("Tap to start")
                            .setFontStyle(
                                LayoutElementBuilders.FontStyle.Builder()
                                    .setColor(ColorBuilders.argb(0xFF999999.toInt()))
                                    .setSize(DimensionBuilders.sp(11f))
                                    .build()
                            ).build()
                    )
                    .build()
            )
            .build()

        return TileBuilders.Tile.Builder()
            .setResourcesVersion("1")
            .setTileTimeline(
                TimelineBuilders.Timeline.Builder()
                    .addTimelineEntry(
                        TimelineBuilders.TimelineEntry.Builder()
                            .setLayout(
                                LayoutElementBuilders.Layout.Builder()
                                    .setRoot(layout)
                                    .build()
                            )
                            .build()
                    )
                    .build()
            )
            .build()
    }
}
