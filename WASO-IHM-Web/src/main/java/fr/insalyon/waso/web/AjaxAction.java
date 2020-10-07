package fr.insalyon.waso.web;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fr.insalyon.waso.util.JsonHttpClient;
import fr.insalyon.waso.util.JsonServletHelper;
import fr.insalyon.waso.util.exception.ServiceException;
import fr.insalyon.waso.util.exception.ServiceIOException;
import java.io.IOException;
import java.text.SimpleDateFormat;

/**
 *
 * @author WASO Team
 */
public class AjaxAction {

    protected String smaUrl;
    protected JsonObject container;

    protected JsonHttpClient jsonHttpClient;

    protected static final SimpleDateFormat FULL_DATE_FORMAT = new SimpleDateFormat("dd/MM/YYYY");
    protected static final SimpleDateFormat FULL_DATETIME_FORMAT = new SimpleDateFormat("dd/MM/YYYY @ HH'h'mm");

    public AjaxAction(String smaUrl, JsonObject container) {
        this.smaUrl = smaUrl;
        this.container = container;

        this.jsonHttpClient = new JsonHttpClient();
    }

    public void release() {
        try {
            this.jsonHttpClient.close();
        } catch (IOException ex) {
            // Ignorer
        }
    }

    protected static JsonObject transformClient(JsonObject client) {

        JsonObject jsonItem = new JsonObject();

        jsonItem.addProperty("id", client.get("id").getAsString());
        jsonItem.addProperty("denomination", client.get("denomination").getAsString());

        String ville = client.get("ville").getAsString();
        int indexCodePostal = ville.lastIndexOf(" ");
        if (indexCodePostal > 0) {
            ville = ville.substring(indexCodePostal + 1) + " " + ville.substring(0, indexCodePostal);

        }

        jsonItem.addProperty("adresse", client.get("adresse").getAsString());
        jsonItem.addProperty("ville", ville);

        if (client.has("personnes")) {

            JsonArray persons = new JsonArray();

            for (JsonElement p : client.get("personnes").getAsJsonArray()) {

                JsonObject person = p.getAsJsonObject();

                JsonObject jsonSubItem = new JsonObject();
                jsonSubItem.add("id", person.get("id"));
                jsonSubItem.add("nom", person.get("nom"));
                jsonSubItem.add("prenom", person.get("prenom"));

                persons.add(jsonSubItem);
            }

            jsonItem.add("personnes", persons);
        }

        return jsonItem;
    }

    protected static JsonArray transformListeClient(JsonArray liste) {

        JsonArray jsonListe = new JsonArray();

        for (JsonElement i : liste) {

            jsonListe.add(transformClient(i.getAsJsonObject()));
        }

        return jsonListe;
    }

    public void getListeClient() throws ServiceException {
        try {
            JsonObject smaResultContainer = null;
            try {
                smaResultContainer = this.jsonHttpClient.post(
                        this.smaUrl,
                        new JsonHttpClient.Parameter("SMA", "getListeClient")
                );
            }
            catch (ServiceIOException ex) {
                throw JsonServletHelper.ServiceMetierCallException(this.smaUrl, "getListeClient", ex);
            }

            JsonArray jsonListe = transformListeClient(smaResultContainer.getAsJsonArray("clients"));

            this.container.add("clients", jsonListe);

        } catch (Exception ex) {
            throw JsonServletHelper.ActionExecutionException("getListeClient", ex);
        }
    }

    public void rechercherClientParNumero(Integer numero) throws ServiceException {
        try {
            JsonObject smaResultContainer = null;
            try {
                smaResultContainer = this.jsonHttpClient.post(
                        this.smaUrl,
                        new JsonHttpClient.Parameter("SMA", "rechercherClientParNumero"),
                        new JsonHttpClient.Parameter("numero", Integer.toString(numero))
                );
            }
            catch (ServiceIOException ex) {
                throw JsonServletHelper.ServiceMetierCallException(this.smaUrl, "rechercherClientParNumero", ex);
            }

            if (smaResultContainer.has("clients")) {
            
                JsonArray jsonListe = transformListeClient(smaResultContainer.getAsJsonArray("clients"));

                this.container.add("clients", jsonListe);
            }

        } catch (IOException ex) {
            throw JsonServletHelper.ActionExecutionException("rechercherClientParNumero", ex);
        }
    }

    void rechercherClientParDenomination(String denomination, String ville) throws ServiceException {
        
        // ...
    }

    void rechercherClientParNomPersonne(String nomPersonne, String ville) throws ServiceException {
        
        // ...
    }

}
