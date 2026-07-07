package diario.storage;

import diario.model.*;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

public class Armazenamento {
    private final Path diretorioRaiz;

    public Armazenamento(Path diretorioRaiz) {
        this.diretorioRaiz = diretorioRaiz;
        try {
            Files.createDirectories(diretorioRaiz);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Listar cadernos (pastas)
    public List<String> listarCadernos() {
        List<String> nomes = new ArrayList<>();
        File[] pastas = diretorioRaiz.toFile().listFiles(File::isDirectory);
        if (pastas != null) {
            for (File f : pastas) {
                nomes.add(f.getName());
            }
        }
        return nomes;
    }

    // Carregar um caderno (lista de páginas)
    public Caderno carregarCaderno(String nome) {
        Path dir = diretorioRaiz.resolve(nome);
        if (!Files.isDirectory(dir)) return null;
        Caderno cad = new Caderno(nome);
        File[] arquivos = dir.toFile().listFiles((d, name) -> name.endsWith(".txt"));
        if (arquivos != null) {
            for (File f : arquivos) {
                String titulo = f.getName().replace(".txt", "");
                String conteudo = lerArquivo(f);
                Pagina p = new Pagina(titulo);
                p.setConteudo(conteudo);
                cad.adicionarPagina(p);
            }
        }
        return cad;
    }

    // Salvar uma página
    public void salvarPagina(Caderno caderno, Pagina pagina) {
        Path dir = diretorioRaiz.resolve(caderno.getNome());
        try {
            Files.createDirectories(dir);
            Path arquivo = dir.resolve(pagina.getTitulo() + ".txt");
            Files.writeString(arquivo, pagina.getConteudo());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String lerArquivo(File f) {
        try {
            return Files.readString(f.toPath());
        } catch (IOException e) {
            return "";
        }
    }
}