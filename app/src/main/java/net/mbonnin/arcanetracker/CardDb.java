package net.mbonnin.arcanetracker;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;

public class CardDb {
    private static ArrayList<Card> sCardList;

    private static String getAssetName(String lang) {
        return "cards_" + lang + ".json";
    }

    public static Card getCard(int dbfId) {
        if (sCardList == null) {
            return null;
        }

        for (Card card: sCardList) {
            if (card.dbfId == dbfId) {
                return card;
            }
        }

        return null;
    }

    public static Card getCard(String key) {
        if (sCardList == null) {
            /*
             * can happen  the very first launch
             * or maybe even later in some cases, the calling code does not check for null so we need to be robust to that
             */
            return Card.UNKNOWN;
        }
        int index = Collections.binarySearch(sCardList, key);
        if (index < 0) {
            return Card.UNKNOWN;
        } else {
            return sCardList.get(index);
        }
    }

    public static ArrayList<Card> getCards() {
        if (sCardList == null) {
            return new ArrayList<>();
        }
        return sCardList;
    }

    public static void init() {
        String jsonName = Language.getCurrentLanguage().jsonName;

        String cards = getStoredJson(jsonName);
        ArrayList<Card> list = new Gson().fromJson(cards, new TypeToken<ArrayList<Card>>() {}.getType());
        if (list == null) {
            list = new ArrayList<>();
        }

        /*
         * these are 3 fake cards needed for CardRender
         */
        list.add(Card.secret("PALADIN"));
        list.add(Card.secret("HUNTER"));
        list.add(Card.secret("MAGE"));
        Collections.sort(list, (a, b) -> a.id.compareTo(b.id));

        sCardList = list;
    }

    private static String getStoredJson(String lang) {
        InputStream inputStream;
        try {
            inputStream = ArcaneTrackerApplication.getContext().getAssets().open(getAssetName(lang));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        try {
            return Utils.inputStreamToString(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
