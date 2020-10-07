package fr.insalyon.waso.som.personne;

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

    public void getListePersonne() throws ServiceException {
        try {
            List<Object[]> listePersonne = this.dBConnection.launchQuery("SELECT PersonneID, Nom, Prenom, Mail FROM PERSONNE ORDER BY PersonneID");

            JsonArray jsonListe = new JsonArray();

            for (Object[] row : listePersonne) {
                JsonObject jsonItem = new JsonObject();

                jsonItem.addProperty("id", (Integer) row[0]);
                jsonItem.addProperty("nom", (String) row[1]);
                jsonItem.addProperty("prenom", (String) row[2]);
                jsonItem.addProperty("mail", (String) row[3]);

                jsonListe.add(jsonItem);
            }

            this.container.add("personnes", jsonListe);

        } catch (DBException ex) {
            throw JsonServletHelper.ServiceObjectMetierExecutionException("Personne", "getListePersonne", ex);
        }
    }
    
    public void rechercherPersonneParNom(String nomPersonne) throws ServiceException {
        try {
            List<Object[]> listePersonne = this.dBConnection.launchQuery("SELECT PersonneID, Nom, Prenom, Mail FROM PERSONNE WHERE Nom ='"+nomPersonne+"'");

            JsonArray jsonListe = new JsonArray();

            for (Object[] row : listePersonne) {
                JsonObject jsonItem = new JsonObject();

                jsonItem.addProperty("id", (Integer) row[0]);
                jsonItem.addProperty("nom", (String) row[1]);
                jsonItem.addProperty("prenom", (String) row[2]);
                jsonItem.addProperty("mail", (String) row[3]);

                jsonListe.add(jsonItem);
            }

            this.container.add("personnes", jsonListe);

        } catch (DBException ex) {
            throw JsonServletHelper.ServiceObjectMetierExecutionException("Personne", "getPersonneParNom", ex);
        }
    }
    
    public void rechercherPersonneParId(int idPersonne) throws ServiceException {
        try {
            List<Object[]> listePersonne = this.dBConnection.launchQuery("SELECT PERSONNEID, Nom, Prenom FROM PERSONNE WHERE PERSONNEID =?",idPersonne);

            JsonArray jsonListe = new JsonArray();

            for (Object[] row : listePersonne) {
                JsonObject jsonItem = new JsonObject();

                jsonItem.addProperty("id", (Integer) row[0]);
                jsonItem.addProperty("nom", (String) row[1]);
                jsonItem.addProperty("prenom", (String) row[2]);

                jsonListe.add(jsonItem);
            }

            this.container.add("personnes", jsonListe);

        } catch (DBException ex) {
            throw JsonServletHelper.ServiceObjectMetierExecutionException("Personne", "rechercherPersonneParId", ex);
        }
    }

}
