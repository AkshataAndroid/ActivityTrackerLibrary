package com.example.activitytrackerlibrary;

enum  InAppType {

    AppTypeHTML("html"),
    AppTypeCoverHTML("coverHtml"),
    AppTypeInterstitialHTML("interstitialHtml"),
    AppTypeHeaderHTML("headerHtml"),
    AppTypeFooterHTML("footerHtml"),
    AppTypeHalfInterstitialHTML("halfInterstitialHtml"),
    AppTypeCover("cover"),
    AppTypeInterstitial("interstitial"),
    AppTypeHalfInterstitial("half-interstitial"),
  AppTypeHeader("header-template"),
    AppTypeFooter("footer-template"),
    AppTypeAlert("alert-template"),
    AppTypeCoverImageOnly("cover-image"),
    AppTypeInterstitialImageOnly("interstitial-image"),
    AppTypeHalfInterstitialImageOnly("half-interstitial-image");


    private final String inAppType;
    InAppType(String type) {
        this.inAppType = type;
    }


    @SuppressWarnings({"unused"})
    static InAppType fromString(String type) {
        switch(type){
            case "html" : {
                return AppTypeHTML;
            }
            case "coverHtml" : {
                return AppTypeCoverHTML;
            }
            case "interstitialHtml" : {
                return AppTypeInterstitialHTML;
            }
            case "headerHtml" : {
                return AppTypeHeaderHTML;
            }
            case "footerHtml" : {
                return AppTypeFooterHTML;
            }
            case "halfInterstitialHtml" : {
                return AppTypeHalfInterstitialHTML;
            }
            case "half-interstitial" : {
                return AppTypeHalfInterstitial;
            }
            case "interstitial" : {
                return AppTypeInterstitial;
            }
            case "cover" : {
                return AppTypeCover;
            }
            case "header-template" : {
                return AppTypeHeader;
            }
            case "footer-template" : {
                return AppTypeFooter;
            }
            case "alert-template" : {
                return AppTypeAlert;
            }
            case "cover-image" : {
                return AppTypeCoverImageOnly;
            }
            case "interstitial-image" : {
                return AppTypeInterstitialImageOnly;
            }
            case "half-interstitial-image" : {
                return AppTypeHalfInterstitialImageOnly;
            }
            default: return null;
        }
    }

    @Override
    public String toString() {
        return inAppType;
    }

}

