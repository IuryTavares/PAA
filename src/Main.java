// Java program for implementation of Ford Fulkerson algorithm 
import java.util.*;
import java.lang.*;
import java.io.*;
import java.util.LinkedList;

class MaxFlow
{
    static final int V = 8;    //Número de vertices do grafo

    /* Retorna true se houver um caminho da origem 's' ao destino
      't' no grafo residual. também preenche parent[] para guardar o
      caminho */
    boolean bfs(int rGraph[][], int s, int t, int parent[])
    {
        // Cria um array (visited[]) e marca todos os vertices  que
        // não foram visitados
        boolean visited[] = new boolean[V];
        for(int i=0; i<V; ++i)
            visited[i]=false;

        // cria uma fila(queue), enfileira o vertice da origem e marca
        // o vertice da origem como visitado
        LinkedList<Integer> queue = new LinkedList<Integer>();
        queue.add(s);
        visited[s] = true;
        parent[s]=-1;

        // começa o loop BFS(busca em largura)
        while (queue.size()!=0)
        {
            int u = queue.poll();

            for (int v=0; v<V; v++)
            {
                //se o nó não tiver sido visitado e o fluxo for maior que 0
                if (visited[v]==false && rGraph[u][v] > 0)
                {
                    queue.add(v);
                    parent[v] = u;
                    visited[v] = true;
                }
            }
        }

        // se  chegamos ao destino no BFS a partir da origem, então
        // retornamos verdade, senão falso
        return (visited[t] == true);
    }


    //Retorna o fluxo máximo de s para t no dado grafo
    int fordFulkerson(int graph[][], int s, int t)
    {
        int u, v;

        // cria uma grafo residual e preencha o grafo residual
        // com as capacidades dadas no grafo original como
        // capacidades residuais no grafo residual

        // O grafo residual rGraph [i][j] indica
        // a capacidade residual da borda i para j
        // se houver uma borda. senão rGraph[i][j] = 0
        int rGraph[][] = new int[V][V];

        for (u = 0; u < V; u++)
            for (v = 0; v < V; v++)
                rGraph[u][v] = graph[u][v];

        //Esta matriz é preenchida pelo BFS  para armazenar o caminho
        int parent[] = new int[V];

        int max_flow = 0;  // Não há fluxo inicialmente

        // aumenta o fluxo enquanto houver caminho da origem até
        // o destino
        while (bfs(rGraph, s, t, parent))
        {
            // encontre a capaciade residual mínima
            // das faixas ao longo do caminho preechido pelo BFS
            // ou seja, encontra o fluxo maximo atraves do caminho encontrado
            int path_flow = Integer.MAX_VALUE;
            for (v=t; v!=s; v=parent[v])
            {
                u = parent[v];
                path_flow = Math.min(path_flow, rGraph[u][v]);
            }

            // atualiza as capaciadedes residuais das bordas e inverte as bordas
            // ao longo do caminho
            for (v=t; v != s; v=parent[v])
            {
                u = parent[v];
                rGraph[u][v] -= path_flow;
                rGraph[v][u] += path_flow;
            }

            // adiciona o fluxo do caminho ao fluxo geral
            max_flow += path_flow;
        }

        // Retorna o fluxo maximo
        return max_flow;
    }

    // Driver program to test above functions 
    public static void main (String[] args) throws java.lang.Exception
    {
        // vamos criar o grafo mostrado no slide
        int graph[][] =new int[][] {  {0, 13, 10, 10, 0, 0, 0, 0},
                                      {0, 0, 0, 0, 24, 0, 0, 0},
                                      {0, 5, 0, 15, 0, 0, 7, 0},
                                      {0, 0, 0, 0, 0, 0, 15, 0},
                                      {0, 0, 0, 0, 0, 1, 0, 9},
                                      {0, 0, 0, 0, 0, 0, 6, 13},
                                      {0, 0, 0, 0, 0, 0, 0, 16},
                                      {0, 0, 0, 0, 0, 0, 0, 0}
        };
        MaxFlow m = new MaxFlow();

        System.out.println("O fluxo máximo possível é: " +
                m.fordFulkerson(graph, 0, 7));

    }
} 