package com.example.grocerymanager.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

data class PurchaseWithItems(
    @Embedded val purchase: PurchaseEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "purchaseId",
    )
    val items: List<PurchaseItemEntity>,
)
