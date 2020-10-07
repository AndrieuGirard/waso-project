package fr.insalyon.waso.sma;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fr.insalyon.waso.util.JsonHttpClient;
import fr.insalyon.waso.util.JsonServletHelper;
import fr.insalyon.waso.util.exception.ServiceException;
import fr.insalyon.waso.util.exception.ServiceIOException;
import java.io.IOException;
import java.util.HashMap;

/**
 *
 * @author WASO Team
 */
public class ServiceMetier {

    protected final String somClientUrl;
    protected final String somPersonneUrl;
    protected final String somContactUrl;
    protected final String somStructureUrl;
    protected final String somProduitUrl;
    protected final String somContratUrl;
    protected final JsonObject container;

    protected JsonHttpClient jsonHttpClient;

    public ServiceMetier(String somClientUrl, String somPersonneUrl, String somContactUrl, String somStructureUrl, String somProduitUrl, String somContratUrl, JsonObject container) {
        this.somClientUrl = somClientUrl;
        this.somPersonneUrl = somPersonneUrl;
        this.somContactUrl = somContactUrl;
        this.somStructureUrl = somStructureUrl;
        this.somProduitUrl = somProduitUrl;
        this.somContratUrl = somContratUrl;
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

    public void getListeClient() throws ServiceException {
        try {

            // 1. Obtenir la liste des Clients
            
            JsonObject clientContainer = null;
            try {
                clientContainer = this.jsonHttpClient.post(
                        this.somClientUrl,
                        new JsonHttpClient.Parameter("SOM", "getListeClient")
                );
            }
            catch (ServiceIOException ex) {
                throw JsonServletHelper.ServiceObjectMetierCallException(this.somClientUrl, "Client", "getListeClient", ex);
            }

            JsonArray jsonOutputClientListe = clientContainer.getAsJsonArray("clients");


            // 2. Obtenir la liste des Personnes
            
            JsonObject personneContainer = null;
            try {
                personneContainer = this.jsonHttpClient.post(
                        this.somPersonneUrl,
                        new JsonHttpClient.Parameter("SOM", "getListePersonne")
                );
            }
            catch (ServiceIOException ex) {
                throw JsonServletHelper.ServiceObjectMetierCallException(this.somPersonneUrl, "Personne", "getListePersonne", ex);
            }


            // 3. Indexer la liste des Personnes
            
            HashMap<Integer, JsonObject> personnes = new HashMap<Integer, JsonObject>();

            for (JsonElement p : personneContainer.getAsJsonArray("personnes")) {

                JsonObject personne = p.getAsJsonObject();

                personnes.put(personne.get("id").getAsInt(), personne);
            }


            // 4. Construire la liste des Personnes pour chaque Client (directement dans le JSON)

            for (JsonElement clientJsonElement : jsonOutputClientListe.getAsJsonArray()) {

                JsonObject client = clientJsonElement.getAsJsonObject();

                JsonArray personnesID = client.get("personnes-ID").getAsJsonArray();

                JsonArray outputPersonnes = new JsonArray();

                for (JsonElement personneID : personnesID) {
                    JsonObject personne = personnes.get(personneID.getAsInt());
                    outputPersonnes.add(personne);
                }

                client.add("personnes", outputPersonnes);

            }


            // 5. Ajouter la liste de Clients au conteneur JSON

            this.container.add("clients", jsonOutputClientListe);

        } catch (Exception ex) {
            throw JsonServletHelper.ServiceMetierExecutionException("getListeClient", ex);
        }
    }
    
    
     public void rechercherClientParDenomination(String denomination,String ville) throws ServiceException {
        try {

            // 1. Obtenir la liste des Clients
            
            
            JsonObject clientContainer = null;
            try {
                clientContainer = this.jsonHttpClient.post(
                        this.somClientUrl,
                        new JsonHttpClient.Parameter("SOM", "rechercherClientParDenomination"),
                        new JsonHttpClient.Parameter("denom", denomination),
                        new JsonHttpClient.Parameter("city", ville)
                );
            }
            catch (ServiceIOException ex) {
                throw JsonServletHelper.ServiceObjectMetierCallException(this.somClientUrl, "Client", "getListeClient", ex);
            }

            JsonArray jsonOutputClientListe = clientContainer.getAsJsonArray("clients");

            System.out.println(jsonOutputClientListe);
            // 2. Obtenir les idPersonnes
            
            for (JsonElement e : jsonOutputClientListe.getAsJsonArray()) {
                JsonArray outputPersonnes = new JsonArray();
                JsonObject client = e.getAsJsonObject();

                JsonArray personnesID = client.get("personnes-ID").getAsJsonArray();

                
                  
                for (JsonElement personneID : personnesID) {
                
                    JsonObject personneContainer = new JsonObject();
                    try {
                        personneContainer = this.jsonHttpClient.post(
                        this.somPersonneUrl,
                        new JsonHttpClient.Parameter("SOM", "rechercherPersonneParId"),
                        new JsonHttpClient.Parameter("id-personne", personneID.getAsString())
                        );
                    }
                    catch (ServiceIOException ex) {
                        throw JsonServletHelper.ServiceObjectMetierCallException(this.somClientUrl, "Client", "getListeClient", ex);
                    }
                    outputPersonnes.add( personneContainer);
                }
                
                client.add("personnes", outputPersonnes);
            }

            // 5. Ajouter la liste de Clients au conteneur JSON

            this.container.add("personnes", jsonOutputClientListe);
            

        } catch (Exception ex) {
            throw JsonServletHelper.ServiceMetierExecutionException("rechercherClientParDenomination", ex);
        }
    }

}
