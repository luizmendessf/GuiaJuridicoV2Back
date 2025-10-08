package org.guiajuridico.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.guiajuridico.model.Oportunidade;
import org.guiajuridico.repository.OportunidadeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

@Component
public class MigracaoOportunidades implements CommandLineRunner {

    @Autowired
    private OportunidadeRepository oportunidadeRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void run(String... args) throws Exception {
        // Verifica se já existem oportunidades no banco para evitar duplicação
        long count = oportunidadeRepository.count();
        if (count > 2) {
            System.out.println("Migração já foi executada. Oportunidades já existem no banco.");
            return;
        }

        System.out.println("Iniciando migração das oportunidades do JSON para o banco de dados...");

        try {
            // Lê o arquivo JSON do classpath
            ClassPathResource resource = new ClassPathResource("static/oportunidade.json");
            InputStream inputStream = resource.getInputStream();
            
            // Parse do JSON
            JsonNode rootNode = objectMapper.readTree(inputStream);
            
            List<Oportunidade> oportunidades = new ArrayList<>();
            
            for (JsonNode node : rootNode) {
                Oportunidade oportunidade = new Oportunidade();
                
                // Mapeia os campos básicos
                oportunidade.setTitle(node.get("title").asText());
                oportunidade.setCompany(node.get("company").asText());
                oportunidade.setLocation(node.get("location").asText());
                oportunidade.setType(node.get("type").asText());
                oportunidade.setStatus(node.get("status").asText());
                oportunidade.setDescription(node.get("description").asText());
                oportunidade.setSalary(node.get("salary").asText());
                oportunidade.setImage(node.get("image").asText());
                oportunidade.setApplicationLink(node.get("applicationLink").asText());
                
                // Converte datas
                if (node.has("openingDate") && !node.get("openingDate").isNull()) {
                    oportunidade.setOpeningDate(Date.valueOf(node.get("openingDate").asText()));
                }
                if (node.has("closingDate") && !node.get("closingDate").isNull()) {
                    oportunidade.setClosingDate(Date.valueOf(node.get("closingDate").asText()));
                }
                
                // Converte requirements para JSON string
                if (node.has("requirements") && !node.get("requirements").isNull()) {
                    JsonNode requirementsNode = node.get("requirements");
                    if (requirementsNode.isArray()) {
                        // Se é array, converte diretamente
                        oportunidade.setRequirements(objectMapper.writeValueAsString(requirementsNode));
                    } else if (requirementsNode.isTextual()) {
                        // Se é string, cria um array com um elemento
                        List<String> requirementsList = new ArrayList<>();
                        requirementsList.add(requirementsNode.asText());
                        oportunidade.setRequirements(objectMapper.writeValueAsString(requirementsList));
                    }
                }
                
                oportunidades.add(oportunidade);
            }
            
            // Salva todas as oportunidades no banco
            oportunidadeRepository.saveAll(oportunidades);
            
            System.out.println("Migração concluída com sucesso! " + oportunidades.size() + " oportunidades foram inseridas no banco de dados.");
            
        } catch (Exception e) {
            System.err.println("Erro durante a migração: " + e.getMessage());
            e.printStackTrace();
        }
    }
}