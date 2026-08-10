package org.guiajuridico.service;

import org.guiajuridico.model.Oportunidade;
import org.guiajuridico.model.Usuario;
import org.guiajuridico.repository.OportunidadeRepository;
import org.guiajuridico.repository.OportunidadeSpecification;
import org.guiajuridico.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class OportunidadeService {

    @Autowired
    private OportunidadeRepository oportunidadeRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    // Método para a listagem completa e sem filtros
    public List<Oportunidade> listarTodas() {
        return oportunidadeRepository.findAll();
    }

    public List<Oportunidade> listarComFiltros(String tipo, String status, String termo) {
        // Lista para guardar nossas especificações (filtros)
        List<Specification<Oportunidade>> specs = new ArrayList<>();

        // Adiciona a condição de tipo, se ela for fornecida
        if (tipo != null && !tipo.equalsIgnoreCase("Todos")) {
            specs.add(OportunidadeSpecification.porTipo(tipo));
        }

        // Adiciona a condição de status, se ela for fornecida
        if (status != null && !status.equalsIgnoreCase("Todos")) {
            specs.add(OportunidadeSpecification.porStatus(status));
        }

        // Adiciona a condição de busca por termo, se ele for fornecido
        if (termo != null && !termo.isEmpty()) {
            specs.add(OportunidadeSpecification.porTermoDeBusca(termo));
        }

        // Combina todas as especificações com "E" (AND)
        // Esta é a forma moderna que substitui o .where(null)
        Specification<Oportunidade> specFinal = Specification.allOf(specs);

        // Executa a busca no repositório com as especificações combinadas
        return oportunidadeRepository.findAll(specFinal);
    }

    public Oportunidade criarOportunidade(Oportunidade oportunidade) {
        Usuario usuarioLogado = getUsuarioLogado();
        oportunidade.setCriadoPor(usuarioLogado);
        oportunidade.setAtualizadoPor(usuarioLogado);
        return oportunidadeRepository.save(oportunidade);
    }

    public void deletarOportunidade(Integer id) {
        oportunidadeRepository.deleteById(id);
    }

    public Optional<Oportunidade> buscarPorId(Integer id) {
        return oportunidadeRepository.findById(id);
    }

    public Optional<Oportunidade> atualizarOportunidade(Integer id, Oportunidade oportunidadeAtualizada) {
        Usuario usuarioLogado = getUsuarioLogado();
        return oportunidadeRepository.findById(id).map(oportunidadeExistente -> {
            oportunidadeExistente.setTitle(oportunidadeAtualizada.getTitle());
            oportunidadeExistente.setCompany(oportunidadeAtualizada.getCompany());
            oportunidadeExistente.setLocation(oportunidadeAtualizada.getLocation());
            oportunidadeExistente.setDescription(oportunidadeAtualizada.getDescription());
            oportunidadeExistente.setType(oportunidadeAtualizada.getType());
            oportunidadeExistente.setImage(oportunidadeAtualizada.getImage());
            oportunidadeExistente.setRequirements(oportunidadeAtualizada.getRequirements());
            oportunidadeExistente.setSalary(oportunidadeAtualizada.getSalary());
            oportunidadeExistente.setApplicationLink(oportunidadeAtualizada.getApplicationLink());
            oportunidadeExistente.setOpeningDate(oportunidadeAtualizada.getOpeningDate());
            oportunidadeExistente.setClosingDate(oportunidadeAtualizada.getClosingDate());
            oportunidadeExistente.setStatus(oportunidadeAtualizada.getStatus());
            oportunidadeExistente.setAtualizadoPor(usuarioLogado);

            return oportunidadeRepository.save(oportunidadeExistente);
        });
    }

    private Usuario getUsuarioLogado() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof UserDetails userDetails)) {
            throw new RuntimeException("Usuário não autenticado");
        }
        return usuarioRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
    }
}
