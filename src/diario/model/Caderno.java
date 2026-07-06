package diario.model;

import java.util.ArrayList;
import java.util.List;

public class Caderno {
    private String nome;
    private List<Pagina> paginas;

    public Caderno(String nome) {
        this.nome = nome;
        this.paginas = new ArrayList<>();
    }

    public void adicionarPagina(Pagina p) {
        paginas.add(p);
    }

    public void removerPagina(Pagina p) {
        paginas.remove(p);
    }

    // getters
    public String getNome() { return nome; }
    public List<Pagina> getPaginas() { return paginas; }
}
