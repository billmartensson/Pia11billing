package se.magictechnology.pia11billing

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import com.android.billingclient.api.*
import com.android.billingclient.api.BillingClient.BillingResponseCode
import com.android.billingclient.api.BillingClient.ProductType

/*
    Skapa BillingClient
    BillingClient connect
    Hämta produkter
    ...
    Köp av produkt
    Godkänna köp
 */


class MainActivity : AppCompatActivity() {

    var theproducts : List<ProductDetails>? = null

    private val purchasesUpdatedListener =
        PurchasesUpdatedListener { billingResult, purchases ->
            Log.i("PIA11DEBUG", "PURCHASE UPDATE")
            if(billingResult.responseCode == BillingResponseCode.OK) {
                Log.i("PIA11DEBUG", "KÖP OK")

                for(purprod in purchases!!) {
                    for(pprod in purprod.products) {
                        Log.i("PIA11DEBUG", "KÖPT " + pprod)
                        // Här spara firebase/server osv...
                        if(pprod == "pia11premium") {
                            confirmPremium(purprod)
                        }
                        if(pprod == "pia11credit") {
                            confirmCredit(purprod)
                        }
                    }
                }

            } else {
                Log.i("PIA11DEBUG", "KÖP EJ OK")


            }
        }

    private lateinit var billingClient : BillingClient


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        billingClient = BillingClient.newBuilder(this)
            .setListener(purchasesUpdatedListener)
            .enablePendingPurchases()
            .build()

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode ==  BillingClient.BillingResponseCode.OK) {
                    Log.i("PIA11DEBUG", "BILLING CONNECT OK")
                    // The BillingClient is ready. You can query purchases here.
                    // Nu kan vi hämta produkter
                    getProducts()
                } else {
                    Log.i("PIA11DEBUG", "BILLING CONNECT NOT OK")
                    Log.i("PIA11DEBUG", billingResult.debugMessage)
                }
            }
            override fun onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
            }
        })

        findViewById<Button>(R.id.prod1buyButton).setOnClickListener {
            buyproduct(theproducts!![0])
        }

        findViewById<Button>(R.id.prod2buyButton).setOnClickListener {
            buyproduct(theproducts!![1])
        }

    }

    fun getProducts() {

        val prod1 = QueryProductDetailsParams.Product.newBuilder()
            .setProductId("pia11premium")
            .setProductType(BillingClient.ProductType.INAPP)
            .build()

        val prod2 = QueryProductDetailsParams.Product.newBuilder()
            .setProductId("pia11credit")
            .setProductType(BillingClient.ProductType.INAPP)
            .build()

        val allproducts = mutableListOf<QueryProductDetailsParams.Product>()
        allproducts.add(prod1)
        allproducts.add(prod2)

        val queryParams = QueryProductDetailsParams.newBuilder().setProductList(allproducts).build()

        billingClient.queryProductDetailsAsync(queryParams) {
                billingResult,
                productDetailsList ->
            // check billingResult
            // process returned productDetailsList

            if(billingResult.responseCode == BillingResponseCode.OK) {
                Log.i("PIA11DEBUG", "PRODUCT RESPONSE OK")

                theproducts = productDetailsList

                Log.i("PIA11DEBUG", "NUMBER OF PRODUCTS " + theproducts!!.size.toString())

                showProducts()
            } else {
                Log.i("PIA11DEBUG", "PRODUCT RESPONSE NOT OK")
            }



        }

    }

    fun showProducts() {
        findViewById<TextView>(R.id.prod1titleTV).text = theproducts!![0].title
        findViewById<TextView>(R.id.prod2titleTV).text = theproducts!![1].title

        findViewById<TextView>(R.id.prod1descTV).text = theproducts!![0].description
        findViewById<TextView>(R.id.prod2descTV).text = theproducts!![1].description

    }

    fun buyproduct(prod : ProductDetails) {
        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                // retrieve a value for "productDetails" by calling queryProductDetailsAsync()
                .setProductDetails(prod)
                .build()
        )

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()

        // Launch the billing flow
        val billingResult = billingClient.launchBillingFlow(this, billingFlowParams)
    }

    fun confirmPremium(purs : Purchase) {
        val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purs.purchaseToken).build()

        billingClient.acknowledgePurchase(acknowledgePurchaseParams) {

        }
    }

    fun confirmCredit(purs : Purchase) {
        val consumeParams =
            ConsumeParams.newBuilder()
                .setPurchaseToken(purs.getPurchaseToken())
                .build()

        billingClient.consumeAsync(consumeParams) { billingresult, resultstring ->

        }
    }

    fun getHistory() {
        val params = QueryPurchaseHistoryParams.newBuilder()
            .setProductType(ProductType.INAPP).build()

        billingClient.queryPurchaseHistoryAsync(params) { billingresult, purchasehistory ->

            if(purchasehistory != null) {
                for(phist in purchasehistory) {
                    for(prod in phist.products) {
                        Log.i("PIA11DEBUG", "KÖPT PRODUKT " + prod)
                    }
                }
            }

        }
    }

}