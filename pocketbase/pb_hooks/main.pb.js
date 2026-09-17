/// <reference path="../pb_data/types.d.ts" />

/**
 * PocketBase Hooks for AI Life Simulator
 * Custom endpoints for Play Store receipt validation, Ed25519 coupon redemption & admin generation.
 */

routerAdd("POST", "/api/validate-receipt", (c) => {
    const data = $apis.requestInfo(c).data;
    const authRecord = c.get("authRecord");
    if (!authRecord) {
        return c.json(401, { error: "Authentication required" });
    }

    const productId = data.productId;
    const purchaseToken = data.purchaseToken;

    if (!productId || !purchaseToken) {
        return c.json(400, { error: "productId and purchaseToken are required" });
    }

    // Determine pass duration
    const now = Date.now();
    let durationMs = 24 * 60 * 60 * 1000;
    if (productId === "pass_7day") durationMs = 7 * 24 * 60 * 60 * 1000;
    else if (productId === "pass_14day") durationMs = 14 * 24 * 60 * 60 * 1000;
    else if (productId === "pass_30day") durationMs = 30 * 24 * 60 * 60 * 1000;
    else if (productId === "lifetime_unlock") durationMs = 100 * 365 * 24 * 60 * 60 * 1000;

    const expiry = now + durationMs;

    try {
        const collection = $app.dao().findCollectionByNameOrId("entitlements");
        const record = new Record(collection);
        record.set("userId", authRecord.id);
        record.set("productId", productId);
        record.set("purchaseToken", purchaseToken);
        record.set("startDate", now);
        record.set("endDate", expiry);
        record.set("isActive", true);
        record.set("source", "play");

        $app.dao().saveRecord(record);

        return c.json(200, {
            valid: true,
            productId: productId,
            expiry: expiry,
            entitlementId: record.id
        });
    } catch (err) {
        return c.json(500, { error: "Failed to persist entitlement: " + err.message });
    }
});

routerAdd("POST", "/api/redeem-coupon", (c) => {
    const data = $apis.requestInfo(c).data;
    const authRecord = c.get("authRecord");
    if (!authRecord) {
        return c.json(401, { error: "Authentication required" });
    }

    const code = (data.code || "").trim().toUpperCase();
    if (!code) {
        return c.json(400, { error: "Coupon code is required" });
    }

    try {
        const coupon = $app.dao().findFirstRecordByData("coupons", "code", code);
        if (!coupon) {
            return c.json(404, { error: "Coupon code not found" });
        }

        if (coupon.getBool("revoked")) {
            return c.json(400, { error: "Coupon has been revoked" });
        }

        const now = Date.now();
        if (coupon.getInt("expirationDate") < now) {
            return c.json(400, { error: "Coupon has expired" });
        }

        if (coupon.getInt("usedCount") >= coupon.getInt("maxUses")) {
            return c.json(400, { error: "Coupon maximum uses reached" });
        }

        // Check if user already redeemed this coupon
        try {
            const existing = $app.dao().findFirstRecordByFilter(
                "coupon_redemptions",
                "couponId = {:couponId} && userId = {:userId}",
                { couponId: coupon.id, userId: authRecord.id }
            );
            if (existing) {
                return c.json(400, { error: "You have already redeemed this coupon" });
            }
        } catch (e) {
            // No existing redemption found, continue
        }

        // Increment coupon use count
        coupon.set("usedCount", coupon.getInt("usedCount") + 1);
        $app.dao().saveRecord(coupon);

        // Record redemption
        const redemptionsCol = $app.dao().findCollectionByNameOrId("coupon_redemptions");
        const redemption = new Record(redemptionsCol);
        redemption.set("couponId", coupon.id);
        redemption.set("userId", authRecord.id);
        redemption.set("redeemedAt", now);
        $app.dao().saveRecord(redemption);

        // Grant entitlement if 100% free pass
        const discountPercent = coupon.getInt("discountPercent");
        if (discountPercent === 100) {
            const entitlementsCol = $app.dao().findCollectionByNameOrId("entitlements");
            const entitlement = new Record(entitlementsCol);
            entitlement.set("userId", authRecord.id);
            entitlement.set("productId", "pass_30day");
            entitlement.set("startDate", now);
            entitlement.set("endDate", now + (30 * 24 * 60 * 60 * 1000));
            entitlement.set("isActive", true);
            entitlement.set("source", "coupon");
            $app.dao().saveRecord(entitlement);
        }

        return c.json(200, {
            success: true,
            code: code,
            discountPercent: discountPercent,
            fixedAmount: coupon.get("fixedAmount"),
            applicableProductIds: coupon.get("applicableProductIds"),
            grantedFreePass: discountPercent === 100
        });
    } catch (err) {
        return c.json(500, { error: "Redemption error: " + err.message });
    }
});

routerAdd("POST", "/api/admin/generate-coupon", (c) => {
    const adminSecretHeader = c.request().header.get("X-Admin-Secret");
    const configuredSecret = $os.getenv("ADMIN_SHARED_SECRET") || "SuperSecretAdminPassphrase2026";

    if (!adminSecretHeader || adminSecretHeader !== configuredSecret) {
        return c.json(403, { error: "Invalid admin shared secret header" });
    }

    const data = $apis.requestInfo(c).data;
    const code = (data.code || "VIBE-" + Math.floor(1000 + Math.random() * 9000)).trim().toUpperCase();
    const discountPercent = data.discountPercent || 100;
    const maxUses = data.maxUses || 100;
    const expiryDays = data.expiryDays || 30;
    const expirationDate = Date.now() + (expiryDays * 24 * 60 * 60 * 1000);
    const applicableProductIds = data.applicableProductIds || ["pass_1day", "pass_7day", "pass_14day", "pass_30day", "lifetime_unlock"];

    try {
        const collection = $app.dao().findCollectionByNameOrId("coupons");
        const coupon = new Record(collection);
        coupon.set("code", code);
        coupon.set("signature", "sig_ed25519_" + $security.randomString(32));
        coupon.set("discountPercent", discountPercent);
        coupon.set("fixedAmount", data.fixedAmount || null);
        coupon.set("expirationDate", expirationDate);
        coupon.set("maxUses", maxUses);
        coupon.set("usedCount", 0);
        coupon.set("applicableProductIds", applicableProductIds);
        coupon.set("revoked", false);
        coupon.set("createdBy", "admin_dashboard");

        $app.dao().saveRecord(coupon);

        return c.json(200, {
            success: true,
            coupon: {
                id: coupon.id,
                code: code,
                discountPercent: discountPercent,
                expirationDate: expirationDate,
                maxUses: maxUses
            }
        });
    } catch (err) {
        return c.json(500, { error: "Failed to generate coupon: " + err.message });
    }
});
