package fr.insalyon.waso.som.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import fr.insalyon.waso.util.DBConnection;
import fr.insalyon.waso.util.JsonServletHelper;
import fr.insalyon.waso.util.exception.DBException;
import fr.insalyon.waso.util.exception.ServiceException;
import java.util.List;

/**
 *
 * @author WASO Team
 */
public class ServiceObjetMetier {

    protected DBConnection dBConnection;
    protected JsonObject container;

    public ServiceObjetMetier(DBConnection dBConnection, JsonObject container) {
        this.dBConnection = dBConnection;
        this.container = container;
    }
    
    public void release() {
        this.dBConnection.close();
    }

    public void getListeClient() throws ServiceException {
        try {
            JsonArray jsonListe = new JsonArray();

            List<Object[]> listeClients = this.dBConnection.launchQuery("SELECT ClientID, TypeClient, Denomination, Adresse, Ville FROM CLIENT ORDER BY ClientID");

            for (Object[] row : listeClients) {
                JsonObject jsonItem = new JsonObject();

                Integer clientId = (Integer) row[0];
                jsonItem.addProperty("id", clientId);
                jsonItem.addProperty("type", (String) row[1]);
                jsonItem.addProperty("denomination", (String) row[2]);
                jsonItem.addProperty("adresse", (String) row[3]);
                jsonItem.addProperty("ville", (String) row[4]);

                List<Object[]> listePersonnes = this.dBConnection.launchQuery("SELECT ClientID, PersonneID FROM COMPOSER WHERE ClientID = ? ORDER BY ClientID,PersonneID", clientId);
                JsonArray jsonSousListe = new JsonArray();
                for (Object[] innerRow : listePersonnes) {
                    jsonSousListe.add((Integer) innerRow[1]);
                }

                jsonItem.add("personnes-ID", jsonSousListe);

                jsonListe.add(jsonItem);
            }

            this.container.add("clients", jsonListe);

        } catch (DBException ex) {
            throw JsonServletHelper.ServiceObjectMetierExecutionException("Client","getListeClient", ex);
        }
    }
    
    public void rechercherClientParDenomination(String denomination, String ville) throws ServiceException {
        try {
            JsonArray jsonListe = new JsonArray();
            String filtre = (ville!=null)? "AND Ville='"+ville+"'" : "";
            denomination = "%"+denomination+"%";
            List<Object[]> listeClients = this.dBConnection.launchQuery("SELECT ClientID, TypeClient, Denomination, Adresse, Ville FROM CLIENT WHERE Denomination LIKE ?"+filtre, denomination);
            System.out.println("listeClients");
            for (Object[] row : listeClients) {
                JsonObject jsonItem = new JsonObject();

                Integer clientId = (Integer) row[0];
                jsonItem.addProperty("id", clientId);
                jsonItem.addProperty("type", (String) row[1]);
                jsonItem.addProperty("denomination", (String) row[2]);
                jsonItem.addProperty("adresse", (String) row[3]);
                jsonItem.addProperty("ville", (String) row[4]);

                List<Object[]> listePersonnes = this.dBConnection.launchQuery("SELECT ClientID, PersonneID FROM COMPOSER WHERE ClientID = ? ORDER BY ClientID,PersonneID", clientId);
                JsonArray jsonSousListe = new JsonArray();
                for (Object[] innerRow : listePersonnes) {
                    jsonSousListe.add((Integer) innerRow[1]);
                }

                jsonItem.add("personnes-ID", jsonSousListe);

                jsonListe.add(jsonItem);
            }

            this.container.add("clients", jsonListe);

        } catch (DBException ex) {
            throw JsonServletHelper.ServiceObjectMetierExecutionException("Client","getListeClient", ex);
        }
    }

}


