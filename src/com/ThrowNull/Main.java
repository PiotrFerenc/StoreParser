package com.ThrowNull;

import org.jsoup.Jsoup;

import java.io.IOException;
import java.net.URL;

public class Main {

    public static void main(String[] args) throws Exception {

        var mediaStore = new StoreParser(new URL("https://www.mediaexpert.pl/agd/lodowki-i-zamrazarki/lodowki/lodowka-samsung-rb38t774db1-ef-nofrost-czarna"));

        var price = mediaStore.GetPrice();
        System.out.println(price);
    }

    //główny program
    private static class StoreParser {
        private Store _selectedStore;

        StoreParser(URL productUrl) throws Exception {
            var hostName = productUrl.getHost();

            //szukamy jaki sklep chcemy zczytać
            //tu możemy dodać jeszcze metodę która będzie wyciągała tylko nazwę np mediaexpert bez www i pl
            switch (hostName) {
                case "www.mediaexpert.pl": {
                    _selectedStore = new MediaExpertStore(productUrl.toString());
                    break;
                }
                default:
                    throw new Exception("Sklep nieobsługiwany");
            }

            //pobieramy kod strony dlatego że w przyszłości może być tak że będziemy pobierać jeszcze inne dane ze strony
            //organiczany ilsc requestów

        }

        //pobieramy cenę wg wybranego sklepu
//ustawiamy także parser który został zdefiniowany w implementacji sklepu
        public String GetPrice() throws IOException {
            if (_selectedStore == null) throw new NullPointerException();

            return _selectedStore.GetPrice(_selectedStore.HtmlParser);
        }
    }

    //Ustawienia sklepu
    //dodajemy kolejną klasę która będzie posiadała
    //możemy także nadpisać sobię metodę GetPrice jeśli sklep będzie wymagał jakiś dodatkowych operacji
    //definiujemy także jaki mechanizm użyjemy do parsowania html
    //może w przyszłościu uda się znaleźć lepszą bibliotekę to nie
    // będzie trzeba przepisywać całej metody tylko zmienić inplementację interface
    private static class MediaExpertStore extends Store {
        MediaExpertStore(String productUrl) throws IOException {
            Name = "mediaexpert";
            PricePath = "div.main-price";
            ProductUrl = productUrl;
            HtmlParser = new JsoupHtmlParser();

            HtmlDocument = HtmlParser.GetDocumentHtml(productUrl);

        }


//        @Override
//        public String GetPrice(IHtmlParser parser) throws IOException {
//            String price = "0";
//
//            if (ProductUrl != null) {
//                price = parser.GetTextFromFirstElement(ProductUrl, PricePath);
//                price = price.replace("zł","");
//            }
//
//            return price;
//        }
    }

    //Core definicji sklepów
    //tu powinny znaleźć się wszystki współne cechy i wspólne mechanizmy
    public static abstract class Store {
        protected String Name;
        protected String PricePath;
        protected String ProductUrl;
        protected IHtmlParser HtmlParser;
        protected String HtmlDocument;

        public String GetPrice(IHtmlParser parser) throws IOException {
            String price = "0";

            if (ProductUrl != null) {
                price = parser.GetTextFromFirstElement(HtmlDocument, PricePath);
            }

            return price;
        }
    }

    //definicja parsera html
    private interface IHtmlParser {

        String GetDocumentHtml(String url) throws IOException;

        String GetTextFromFirstElement(String html, String elementPath) throws IOException;
    }

    private static class JsoupHtmlParser implements IHtmlParser {

        @Override
        public String GetTextFromFirstElement(String html, String elementPath) throws IOException {
            var document = Jsoup.parse(html);

            if (document != null) {
                var priceElement = document.selectFirst(elementPath);

                if (priceElement != null) {
                    return priceElement.text();
                }
            }

            return null;
        }

        @Override
        public String GetDocumentHtml(String url) throws IOException {
            var document = Jsoup.connect(url).get();
            return document.toString();
        }
    }

}
