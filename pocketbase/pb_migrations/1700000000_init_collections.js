/// <reference path="../pb_data/types.d.ts" />
migrate((db) => {
  const snapshot = [
    {
      "name": "entitlements",
      "type": "base",
      "system": false,
      "schema": [
        { "name": "userId", "type": "text", "required": true },
        { "name": "productId", "type": "text", "required": true },
        { "name": "purchaseToken", "type": "text", "required": false },
        { "name": "startDate", "type": "number", "required": true },
        { "name": "endDate", "type": "number", "required": false },
        { "name": "isActive", "type": "bool", "required": true },
        { "name": "source", "type": "text", "required": true }
      ],
      "listRule": "@request.auth.id != '' && userId = @request.auth.id",
      "viewRule": "@request.auth.id != '' && userId = @request.auth.id",
      "createRule": null, // Admin / Hooks only
      "updateRule": null,
      "deleteRule": null
    },
    {
      "name": "coupons",
      "type": "base",
      "system": false,
      "schema": [
        { "name": "code", "type": "text", "required": true, "unique": true },
        { "name": "signature", "type": "text", "required": true },
        { "name": "discountPercent", "type": "number", "required": false },
        { "name": "fixedAmount", "type": "number", "required": false },
        { "name": "expirationDate", "type": "number", "required": true },
        { "name": "maxUses", "type": "number", "required": true },
        { "name": "usedCount", "type": "number", "required": true },
        { "name": "applicableProductIds", "type": "json", "required": true },
        { "name": "revoked", "type": "bool", "required": true },
        { "name": "createdBy", "type": "text", "required": false }
      ],
      "listRule": null, // Admin only
      "viewRule": null,
      "createRule": null,
      "updateRule": null,
      "deleteRule": null
    },
    {
      "name": "coupon_redemptions",
      "type": "base",
      "system": false,
      "schema": [
        { "name": "couponId", "type": "text", "required": true },
        { "name": "userId", "type": "text", "required": true },
        { "name": "redeemedAt", "type": "number", "required": true }
      ],
      "indexes": [
        "CREATE UNIQUE INDEX `idx_coupon_user` ON `coupon_redemptions` (`couponId`, `userId`)"
      ],
      "listRule": "@request.auth.id != '' && userId = @request.auth.id",
      "viewRule": "@request.auth.id != '' && userId = @request.auth.id",
      "createRule": null,
      "updateRule": null,
      "deleteRule": null
    },
    {
      "name": "life_saves",
      "type": "base",
      "system": false,
      "schema": [
        { "name": "userId", "type": "text", "required": true },
        { "name": "lifeData", "type": "json", "required": true },
        { "name": "npcData", "type": "json", "required": false },
        { "name": "memoryData", "type": "json", "required": false },
        { "name": "updatedAt", "type": "number", "required": true }
      ],
      "listRule": "@request.auth.id != '' && userId = @request.auth.id",
      "viewRule": "@request.auth.id != '' && userId = @request.auth.id",
      "createRule": "@request.auth.id != '' && userId = @request.auth.id",
      "updateRule": "@request.auth.id != '' && userId = @request.auth.id",
      "deleteRule": "@request.auth.id != '' && userId = @request.auth.id"
    },
    {
      "name": "analytics_events",
      "type": "base",
      "system": false,
      "schema": [
        { "name": "userId", "type": "text", "required": false },
        { "name": "eventType", "type": "text", "required": true },
        { "name": "payload", "type": "json", "required": false },
        { "name": "timestamp", "type": "number", "required": true }
      ],
      "listRule": null,
      "viewRule": null,
      "createRule": "", // Allow authenticated and anonymous clients to submit analytics
      "updateRule": null,
      "deleteRule": null
    },
    {
      "name": "admin_audit",
      "type": "base",
      "system": false,
      "schema": [
        { "name": "adminUserId", "type": "text", "required": true },
        { "name": "action", "type": "text", "required": true },
        { "name": "payload", "type": "json", "required": false },
        { "name": "timestamp", "type": "number", "required": true }
      ],
      "listRule": null,
      "viewRule": null,
      "createRule": null,
      "updateRule": null,
      "deleteRule": null
    }
  ];

  snapshot.forEach((colData) => {
    const collection = new Collection(colData);
    $app.dao().saveCollection(collection);
  });
}, (db) => {
  const tables = ["admin_audit", "analytics_events", "life_saves", "coupon_redemptions", "coupons", "entitlements"];
  tables.forEach(t => {
    try {
      const col = $app.dao().findCollectionByNameOrId(t);
      $app.dao().deleteCollection(col);
    } catch (e) {}
  });
});
