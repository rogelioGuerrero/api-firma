package sv.mh.fe.controller;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import javax.validation.Valid;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import sv.mh.fe.business.CertificadoBusiness;
import sv.mh.fe.business.FirmarDocumentoBusiness;
import sv.mh.fe.constantes.Errores;
import sv.mh.fe.constantes.Errores.errores;
import sv.mh.fe.filter.FirmarDocumentoFilter;
import sv.mh.fe.models.CertificadoMH;
import sv.mh.fe.validations.FirmarDocumentoValidations;

@RestController
@RequestMapping("/firmardocumento")
@CrossOrigin(origins = "*", maxAge = 3600)
public class FirmarDocumentoController extends Controller {

	final static Logger logger = LoggerFactory.getLogger(FirmarDocumentoController.class);
	
	@Autowired
	private CertificadoBusiness certificadoBusiness;
	
	@Autowired
	private FirmarDocumentoBusiness business;
	
	@Autowired
	private FirmarDocumentoValidations validation;	 
	
	/**
	 * 
	 * @param filter
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/", method = RequestMethod.POST)
	public ResponseEntity<?> firmar(@Valid @RequestBody FirmarDocumentoFilter filter) {
		try {			
			validation.v5validar(filter);
			if(validation.isValido()) {
				ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
				String dteString;
				try {
					dteString = ow.writeValueAsString(filter.getDteJson());
					JSONObject dteObject = new JSONObject(dteString);					
					if(dteObject != null) {
						logger.info("dteObject != null");
						String firma;
						firma = business.firmarJSONBase64(filter.getCertificadoB64(), filter.getPasswordPri(), dteString);
						
						// Create custom response format
						JSONObject response = new JSONObject();
						response.put("success", true);
						response.put("jws", firma);
						
						return ResponseEntity.ok(response.toMap());
					}										
				} catch (JsonProcessingException e) {
					logger.info(errores.COD_810_CONVERTIR_JSON_A_STRING, e.getMessage());
					return ResponseEntity.ok(mensaje.error(Errores.COD_810_CONVERTIR_JSON_A_STRING));
				} catch (Exception e) {
					logger.info(errores.COD_811_CONVERTIR_STRING_A_JSON, e.getMessage());
					return ResponseEntity.ok(mensaje.error(Errores.COD_811_CONVERTIR_STRING_A_JSON));
				}	
			}else {
				return ResponseEntity.ok(mensaje.error(errores.COD_809_DATOS_REQUERIDOS,validation.getRequeridos()));
			}			
		} catch (Exception e1) {
			logger.error(e1.getMessage());
			return ResponseEntity.ok(mensaje.error(errores.COD_804_ERROR_NO_CATALOGADO, e1.getMessage()));			
		}
	}

	@GetMapping("/debug")
	public String getDebug(){
		String enabled = System.getenv("FIRMA_DEBUG");
		if (enabled == null || !"true".equalsIgnoreCase(enabled)) {
			throw new org.springframework.web.server.ResponseStatusException(
					org.springframework.http.HttpStatus.NOT_FOUND);
		}
		
		String envVarName = "certificado.base64.14012805761025";
		String certificadoEnv = System.getenv(envVarName);
		return "Variable: " + envVarName + "\nLength: " + (certificadoEnv != null ? certificadoEnv.length() : "null");
	}
	
	@GetMapping("/status")
	public String getStatus(){
		return "Application is running...!!";
	}
}
