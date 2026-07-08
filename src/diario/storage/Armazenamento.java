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

    public Path getDiretorioRaiz() {
        return diretorioRaiz;
    }

    // Excluir uma página (apaga o arquivo .txt)
    public void excluirPagina(Caderno caderno, Pagina pagina) {
        Path arquivo = diretorioRaiz.resolve(caderno.getNome()).resolve(pagina.getTitulo() + ".txt");
        try {
            Files.deleteIfExists(arquivo);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Excluir um caderno (apaga a pasta e todas as páginas dentro dela)
    public void excluirCaderno(String nome) {
        Path dir = diretorioRaiz.resolve(nome);
        if (!Files.isDirectory(dir)) return;
        try (var caminhos = Files.walk(dir)) {
            caminhos.sorted(Comparator.reverseOrder())
                    .forEach(p -> {
                        try {
                            Files.delete(p);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean renomearCaderno(String nomeAntigo, String nomeNovo) {
        Path origem = diretorioRaiz.resolve(nomeAntigo);
        Path destino = diretorioRaiz.resolve(nomeNovo);
        if (Files.exists(destino)) return false; // evita sobrescrever outro caderno
        try {
            Files.move(origem, destino);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean renomearPagina(Caderno caderno, Pagina pagina, String novoTitulo) {
        Path dir = diretorioRaiz.resolve(caderno.getNome());
        Path origem = dir.resolve(pagina.getTitulo() + ".txt");
        Path destino = dir.resolve(novoTitulo + ".txt");
        if (Files.exists(destino)) return false;
        try {
            Files.move(origem, destino);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private String lerArquivo(File f) {
        try {
            return Files.readString(f.toPath());
        } catch (IOException e) {
            return "";
        }
    }

    // Adicione este método à classe Armazenamento
    public void salvarCaderno(Caderno caderno) {
        Path dir = diretorioRaiz.resolve(caderno.getNome());
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



}