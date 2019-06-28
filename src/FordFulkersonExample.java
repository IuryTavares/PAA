

import static java.lang.Math.min;

import java.util.ArrayList;
import java.util.List;

public class FordFulkersonExample {

    private static class Edge {
        public int from, to;
        public Edge residual;
        public long flow;
        public final long capacity;

        public Edge(int from, int to, long capacity) {
            this.from = from;
            this.to = to;
            this.capacity = capacity;
        }

        public boolean isResidual() {
            return capacity == 0;
        }

        public long remainingCapacity() {
            return capacity - flow;
        }

        public void augment(long bottleNeck) {
            flow += bottleNeck;
            residual.flow -= bottleNeck;
        }

        public String toString(int s, int t) {
            String u = (from == s) ? "s" : ((from == t) ? "t" : String.valueOf(from));
            String v = (to == s) ? "s" : ((to == t) ? "t" : String.valueOf(to));
            return String.format(
                    "Edge %s -> %s | flow = %3d | capacity = %3d | is residual: %s",
                    u, v, flow, capacity, isResidual());
        }
    }

    private abstract static class NetworkFlowSolverBase {

        // Para evitar estouro, defina o infinito para um valor menor que Long.MAX_VALUE;
        static final long INF = Long.MAX_VALUE / 2;

        // Inputs: n = numeros de nós, s = origem, t = destino
        final int n, s, t;

        // 'visited' e 'visitedToken' são variaveis usadas em grafos para
        // rastrear se um nó foi visitado ou não. Em particaular o no 'i' foi visitado
        // recentemente if (visited[i] == visitedToken) é verdadeiro.
        //Isso é útil porque marcar todos os nós como não visitados
        // simplesmente incrementa o visitadoToken.
        protected int visitedToken = 1;
        protected int[] visited;

        //Indica se o algoritmo de fluxo de rede foi executado. Apenas o solucionador
        //  precisa ser executado uma vez porque sempre produz o mesmo resultado.
        protected boolean solved; // protected é um modificador de acesso que permite que apenas classe que herdem possam acessar

        // O fluxo máximo. Que calculado chamando o método {@link #solve}.
        protected long maxFlow;

        // A lista de adjacências representando o grafo de fluxo.
        protected List<Edge>[] graph;

        /**
         * Cria uma instância de uma solução(solver) de rede de fluxo. Use o método {@link #addEdge} para adicionar bordas ao gráfico.
         *
         * @param n - O número de nós no gráfico, incluindo s e t.
         * @param s - O índice do nó de origem, 0 <= s <n
         * @param t - O índice do nó destino, 0 <= t <n e t! = S
         */
        public NetworkFlowSolverBase(int n, int s, int t) {
            this.n = n;
            this.s = s;
            this.t = t;
            initializeEmptyFlowGraph();
            visited = new int[n];
        }

        //
        //Constrói um gráfico vazio com n nós, incluindo s e t
        private void initializeEmptyFlowGraph() {
            graph = new List[n];
            for (int i = 0; i < n; i++) graph[i] = new ArrayList<Edge>();
        }

        /**
         * Adiciona uma aresta direcionada (e sua aresta residual) ao gráfico de fluxo.
         *
         * @param from - O índice do nó em que a borda direcionada começa em.
         * @param to - O índice do nó na extremidade direcionada termina em.
         * @param capacity - A capacidade da borda
         */
        public void addEdge(int from, int to, long capacity) {
            if (capacity <= 0) throw new IllegalArgumentException("Forward edge capacity <= 0");
            Edge e1 = new Edge(from, to, capacity);
            Edge e2 = new Edge(to, from, 0);
            e1.residual = e2;
            e2.residual = e1;
            graph[from].add(e1);
            graph[to].add(e2);
        }

        /**
         Retorna o gráfico residual após o solver ter sido executado. Isso permite que você inspecione
         {@link Edge # flow} e {@link Edge # capacity} valores de cada borda. Isso é útil se você estiver depurando
         ou quiser descobrir quais bordas foram usadas durante o fluxo máximo.
         */
        public List<Edge>[] getGraph() {
            execute();
            return graph;
        }

        // Retorna o fluxo máximo da fonte para o coletor.
        public long getMaxFlow() {
            execute();
            return maxFlow;
        }

        // Wrapper que garante que so chamamos solve() apenas uma vez wrapper variante de um metodo existente
        private void execute() {
            if (solved) return;
            solved = true;
            solve();
        }

        // Método para implementar o que resolve o problema de fluxo de rede.
        public abstract void solve();
    }

    private static class FordFulkersonDfsSolver extends NetworkFlowSolverBase {

        /**
         *Cria uma instância de um solver de rede de fluxo. Use o método {@link #addEdge}
         * para adicionar bordas ao gráfico.
         *
         * @param n - O número de nós no gráfico, incluindo s e t.
         * @param s - O índice do nó de origem, 0 <= s <n
         * @param t - O índice do nó destino, 0 <= t <n e t! = S
         */
        public FordFulkersonDfsSolver(int n, int s, int t) {
            super(n, s, t);
        }

        //Executa o método Ford-Fulkerson aplicando uma primeira pesquisa de profundidade
        //um meio de encontrar um caminho de aumento.
        @Override
        public void solve() {
            // Encontre o fluxo máximo adicionando todos os fluxos de caminho de aumento.
            for (long f = dfs(s, INF); f != 0; f = dfs(s, INF)) {
                visitedToken++;
                maxFlow += f;
            }
        }

        private long dfs(int node, long flow) {
            // No nó destino, retorna o fluxo do caminho aumentante.
            if (node == t) return flow;

            // Marque o nó atual como visitado.
            visited[node] = visitedToken;

            List<Edge> edges = graph[node];
            for (Edge edge : edges) {
                if (edge.remainingCapacity() > 0 && visited[edge.to] != visitedToken) {
                    long bottleNeck = dfs(edge.to, min(flow, edge.remainingCapacity()));

                    // Se fizermos isso de s -> t (a.k.a bottleNeck> 0),
                    // aumente o fluxo com o valor de gargalo.
                    if (bottleNeck > 0) {
                        edge.augment(bottleNeck);
                        return bottleNeck;
                    }
                }
            }
            return 0;
        }
    }

    /* EXAMPLE */

    public static void main(String[] args) {
        //
        //n é o número de nós, incluindo a origem e o coletor.
        int n = 8;

        int s = n - 2;
        int t = n - 1;

        NetworkFlowSolverBase solver = new FordFulkersonDfsSolver(n, s, t);

        // Edges from source
        solver.addEdge(s, 0, 13);
        solver.addEdge(s, 1, 10);
        solver.addEdge(s, 2, 10);

        // Middle edges
        solver.addEdge(0, 3, 24);
        solver.addEdge(1, 2, 15);
        solver.addEdge(2, 5, 15);
        solver.addEdge(1, 0, 5);//
        solver.addEdge(3, 4, 1);//
        solver.addEdge(4, 5, 6);//
        solver.addEdge(1, 2, 15);//
        solver.addEdge(1, 5, 7);//


        // Edges to sink
        solver.addEdge(3, t, 9);
        solver.addEdge(4, t, 13);
        solver.addEdge(5, t,16);

        // Prints:
        // Maximum Flow is: 26
        System.out.printf("Maximum Flow is: %d\n", solver.getMaxFlow());

        List<Edge>[] resultGraph = solver.getGraph();

        // Displays all edges part of the resulting residual graph.
       // for (List<Edge> edges : resultGraph) for (Edge e : edges) System.out.println(e.toString(s, t));
    }
}