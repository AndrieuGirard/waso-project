package fr.insalyon.waso.sma;

import com.google.gson.JsonObject;
import fr.insalyon.waso.util.JsonServletHelper;
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
//@WebServlet(name = "ServiceMetierServlet", urlPatterns = {"/ServiceMetier"})
public class ServiceMetierServlet extends HttpServlet {

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

            String sma = null;

            String pathInfo = request.getPathInfo();
            if (pathInfo != null) {
                sma = pathInfo.substring(1);
            }
            
            String somParameter = request.getParameter("SMA");
            if (somParameter != null) {
                sma = somParameter;
            }

            JsonObject container = new JsonObject();

            ServiceMetier service = new ServiceMetier(
                    this.getInitParameter("URL-SOM-Client"),
                    this.getInitParameter("URL-SOM-Personne"),
                    this.getInitParameter("URL-SOM-Contact"),
                    this.getInitParameter("URL-SOM-Structure"),
                    this.getInitParameter("URL-SOM-Produit"),
                    this.getInitParameter("URL-SOM-Contrat"),
                    container );

            boolean serviceCalled = true;

            if ("getListeClient".equals(sma)) {

                service.getListeClient();

            } else if ("rechercherClientParNumero".equals(sma)) {

                String numeroParametre = request.getParameter("numero");
                if (numeroParametre == null) {
                    throw new ServiceException("Paramètres incomplets");
                }
                Integer numero = Integer.parseInt(numeroParametre);
                
                // service.rechercherClientParNumero(numero);

            } else if ("rechercherClientParDenomination".equals(sma)) {
                
                // service.rechercherClientParDenomination(denomination,ville);

            } else if ("rechercherClientParNomPersonne".equals(sma)) {
                
                // service.rechercherClientParNomPersonne(nomPersonne,ville);

            } else {

                serviceCalled = false;
            }

            service.release();
            
            if (serviceCalled) {

                JsonServletHelper.printJsonOutput(response, container);

            } else {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Service SMA '" + sma + "' not found");
            }

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
        return "Service Metier Servlet";
    }

}
