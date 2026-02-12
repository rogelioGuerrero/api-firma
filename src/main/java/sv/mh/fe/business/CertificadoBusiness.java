package sv.mh.fe.business;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import sv.mh.fe.constantes.Constantes;
import sv.mh.fe.filter.FirmarDocumentoFilter;
import sv.mh.fe.models.CertificadoMH;
import sv.mh.fe.security.Cryptographic;
import sv.mh.fe.utils.FileUtils;

@Service
public class CertificadoBusiness {
	
	@Autowired
	private Cryptographic cryptographic;
	
	@Autowired
	private FileUtils fileUtilis;
	
	@Value("${certificado.base64.14012805761025:}")
	private String certificadoBase64;
	
	private static Logger logger = LoggerFactory.getLogger(CertificadoBusiness.class);		
	
	public CertificadoMH recuperarCertifiado(FirmarDocumentoFilter filter) throws IOException, NoSuchAlgorithmException {		
		XmlMapper xmlMapper = new XmlMapper();
		JavaTimeModule module = new JavaTimeModule();
		xmlMapper.registerModule(module);

		CertificadoMH certificado = null;
		String crypto = cryptographic.encrypt(filter.getPasswordPri(), Cryptographic.SHA512);
		
		// Intentar obtener certificado desde variable de entorno primero
		String contenido = obtenerContenidoCertificado(filter.getNit());
		
		if (contenido == null) {
			Path path = Paths.get(Constantes.DIRECTORY_UPLOADS,filter.getNit()+".crt");
			contenido = fileUtilis.LeerArchivo(path);
		}
		
		certificado = xmlMapper.readValue(contenido, CertificadoMH.class);
		
		if(certificado.getPrivateKey().getClave().equals(crypto)){
			return certificado;			
		}
		logger.info("Password no valido: "+certificado.getNit());
		return null;
	}
	
	private String obtenerContenidoCertificado(String nit) throws IOException {
		// Verificar si hay certificado en variable de entorno para este NIT específico
		String envVarName = "certificado.base64." + nit;
		String certificadoEnv = System.getenv(envVarName);
		
		if (certificadoEnv != null && !certificadoEnv.isEmpty()) {
			try {
				byte[] certBytes = Base64.getDecoder().decode(certificadoEnv);
				return new String(certBytes);
			} catch (Exception e) {
				logger.error("Error decodificando certificado desde variable de entorno: " + envVarName, e);
				return null;
			}
		}
		
		// Verificar si hay certificado configurado en application.yml para NIT 14012805761025
		if ("14012805761025".equals(nit) && certificadoBase64 != null && !certificadoBase64.isEmpty()) {
			try {
				byte[] certBytes = Base64.getDecoder().decode(certificadoBase64);
				return new String(certBytes);
			} catch (Exception e) {
				logger.error("Error decodificando certificado desde application.yml", e);
				return null;
			}
		}
		
		return null;
	}
}
