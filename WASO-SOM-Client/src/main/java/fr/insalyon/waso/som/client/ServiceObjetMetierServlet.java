package fr.insalyon.waso.som.client;

import com.google.gson.JsonObject;
import fr.insalyon.waso.util.DBConnection;
import fr.insalyon.waso.util.JsonServletHelper;
import fr.insalyon.waso.util.exception.DBException;
import fr.insalyon.waso.util.exception.ServiceException;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author WASO Team
 */
//@WebServlet(name = "ServiceObjetMetierServlet", urlPatterns = {"/ServiceObjetMetier"})
public class ServiceObjetMetierServlet extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding(JsonServletHelper.ENCODING_UTF8);

        try {
            
            String som = null;
            
            // cf. https://codebox.net/pages/java-servlet-url-parts
            String pathInfo = request.getPathInfo();
            if (pathInfo != null) {
                som = pathInfo.substring(1);
            }
            
            String somParameter = request.getParameter("SOM");
            if (somParameter != null) {
                som = somParameter;
            }

            DBConnection connection = new DBConnection(
                    this.getInitParameter("JDBC-Client-URL"),
                    this.getInitParameter("JDBC-Client-User"),
                    this.getInitParameter("JDBC-Client-Password"),
                    "CLIENT", "COMPOSER"
            );

            JsonObject container = new JsonObject();

            ServiceObjetMetier service = new ServiceObjetMetier(connection, container);

            boolean serviceCalled = true;

            if ("getListeClient".equals(som)) {

                service.getListeClient();

            } else if ("rechercherClientParNumero".equals(som)) {

                String numeroParametre = request.getParameter("numero");
                if (numeroParametre == null) {
                    throw new ServiceException("Paramètres incomplets");
                }
                Integer numero = Integer.parseInt(numeroParametre);

                // service.rechercherClientParNumero(numero);

            } else if ("rechercherClientParDenomination".equals(som)) {

                String denomination = request.getParameter("denom");
                String ville = request.getParameter("city");
                service.rechercherClientParDenomination(denomination, ville);

            } else if ("rechercherClientParPersonne".equals(som)) {

                // service.rechercherClientParPersonne(personneIds, ville);

            } else {

                serviceCalled = false;
            }

            if (serviceCalled) {

                JsonServletHelper.printJsonOutput(response, container);

            } else {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Service SOM '" + som + "' not found");
            }
            
            service.release();

        } catch (DBException ex) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "DB Exception: " + ex.getMessage());
            this.getServletContext().log("DB Exception in " + this.getClass().getName(), ex);
        } catch (ServiceException ex) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Service Exception: " + ex.getMessage());
            this.getServletContext().log("Service Exception in " + this.getClass().getName(), ex);
        }
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Service Objet Metier Servlet";
    }

}
