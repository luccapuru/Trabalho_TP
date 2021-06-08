import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.*;
import java.io.*;
import java.util.*;
import javax.swing.Timer;


import static java.awt.event.KeyEvent.*;

public class Servidor {
    static int[][] mapaint;
    public static void main(String[] args) {
        //matriz de inteiros que serve de base pro jogo
        mapaint = new int[][]{
                {-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1},
                {-1,2,0,0,1,1,1,1,1,1,1,0,0 ,0,-1},
                {-1,0,-1,1,-1,1,-1,0,-1,1,-1,1,-1,0,-1},
                {-1,0,1,1,1,1,0,0,0,0,0,1,1,0,-1},
                {-1,1,-1,1,-1,1,-1,1,-1,1,-1,1,-1,0,-1},
                {-1,1,1,0,0,0,0,0,1,0,0,0,1,1,-1},
                {-1,0,-1,1,-1,0,-1,1,-1,0,-1,0,-1,1,-1},
                {-1,0,0,0,1,0,1,1,1,0,0,0,1,0,-1},
                {-1,0,-1,1,-1,0,-1,1,-1,0,-1,1,-1,0,-1},
                {-1,0,1,1,1,0,1,1,1,0,1,1,0,0,-1},
                {-1,1,-1,1,-1,1,-1,1,-1,1,-1,1,-1,1,-1},
                {-1,0,1,1,0,0,0,1,1,1,1,1,1,0,-1},
                {-1,0,-1,1,-1,1,-1,1,-1,1,-1,1,-1,0,-1},
                {-1,0,0,0,1,1,1,1,1,1,1,0,0,3,-1},
                {-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1, -1}
        };
        ServerSocket serverSocket=null;

        try {
            serverSocket = new ServerSocket(80);
        } catch (IOException e) {
            System.out.println("Could not listen on port: " + 80 + ", " + e);
            System.exit(1);
        }
        //for para conectar os dois clientes
        for (int i=0; i<2; i++) {
            Socket clientSocket = null;
            try {
                clientSocket = serverSocket.accept();
            } catch (IOException e) {
                System.out.println("Accept failed: " + 80 + ", " + e);
                System.exit(1);
            }

            System.out.println("Accept Funcionou!");

            new Servindo(clientSocket, i).start();

        }

        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


class Servindo extends Thread {
    Socket clientSocket;
    int id; //identificação dada ao cliente
    static PrintStream os[] = new PrintStream[2]; //vetor de PrintStream para comunicação com os clentes
    static int cont=0; //contador para indice do vetor de os
    static boolean jogador1_bomba = false;
    static boolean jogador2_bomba = false; //variaveis booleanas para impedir os jogadores de plantarem mais de uma bomba cada
    static final int VITORIA = 2; //cte para indicar o numero para a vitória
    static int pontos[] = new int[2]; //vetor para qtd de pontos de cada jogador
    static final int J1VENCEU = 3;
    static final int J2VENCEU = 4; //ctes q indicam qual jogador venceu
    static int vencedor = 0; //variável q guarda quem venceu e se o jogo acabou
    static int pos1x = 1;
    static int pos1y = 1; //posição inicial para jogador 1
    static int pos2x = 13;
    static int pos2y = 13; //posição inicial para jogador 2
    static int tempo = 300;
    static Timer timer;

    Servindo(Socket clientSocket, int id) {
        this.clientSocket = clientSocket;
        this.id = id;
        //construtor recebe o id e atribui ao Cliente
    }

    public void run() {

        int aux; //variavel para receber as teclas vindas do cliente

        int delay = 1000; //delay do timer de 1 segundo
        ActionListener tempoJogo = new ActionListener() { //decrementa o timer
            public void actionPerformed(ActionEvent evt) {
                tempo--;
                envia();
                if(tempo == 0)
                    System.exit(1);
            }
        };
        timer = new Timer(delay, tempoJogo);
        timer.start(); //inicia o timer

        try {
            Scanner is = new Scanner(clientSocket.getInputStream());
            os[cont++] = new PrintStream(clientSocket.getOutputStream());


            do { //do-while do jogo
                System.out.println("\nMatriz impressa no comeco do DO\n");
                for(int x = 0; x < 15; x++) {
                    for(int y = 0; y < 15; y++){
                        System.out.print(" " + Servidor.mapaint[x][y] + " ");
                    }
                    System.out.print("\n");
                }


                aux = is.nextInt(); //recebendo a tecla do cliente

                System.out.println("tecla recebida: " + aux);
                if (id == 0) { //if que vai tratar a tecla mandada pelo JOGADOR 1

                    if (aux == VK_A) { //se apertou A, move-se para esquerda
                        if (Servidor.mapaint[pos1x][pos1y - 1] == -1 || Servidor.mapaint[pos1x][pos1y - 1] == 1) {
                            System.out.println("nao pode ultrapassar bloco");
                        } else if (Servidor.mapaint[pos1x][pos1y - 1] == 0) { //prox posição é caminho, pode andar
                            System.out.println("posicao atual[" + pos1x + "][" + pos1y + "] do jogador " + id + ": " + Servidor.mapaint[pos1x][pos1y]);
                            if(Servidor.mapaint[pos1x][pos1y] == 4){ //se jogador tiver soltado bomba, deixa a imagem da bomba no lugar ao andar
                                Servidor.mapaint[pos1x][pos1y] = 6;
                            }
                            else if(Servidor.mapaint[pos1x][pos1y] == 2){ //se não tiver soltado bomba
                                Servidor.mapaint[pos1x][pos1y] = 0;
                            }
                            pos1y--; //alterando a posição
                            Servidor.mapaint[pos1x][pos1y] = 2; //colocando jogador1 na nova posição
                            System.out.println("posicao nova[" + pos1x + "][" + pos1y + "] do jogador " + id + ": " + Servidor.mapaint[pos1x][pos1y]);
                        }
                        else if (Servidor.mapaint[pos1x][pos1y - 1] == 7 || Servidor.mapaint[pos1x][pos1y - 1] == 8){ //prox posição é explosão, mata o jogador
                            Servidor.mapaint[pos1x][pos1y] = 0;
                            vencedor = Servindo.placar(1); //chama função para atualizar o placar e reseta a posição do jogador1
                        }
                    }

                    if (aux == VK_D) { //se apertou D, move-se para direita
                        if (Servidor.mapaint[pos1x][pos1y + 1] == -1 || Servidor.mapaint[pos1x][pos1y + 1] == 1) {
                            System.out.println("nao pode ultrapassar bloco");
                        } else if (Servidor.mapaint[pos1x][pos1y + 1] == 0) { //prox posição é caminho, pode andar
                            System.out.println("posicao atual[" + pos1x + "][" + pos1y + "] do jogador " + id + ": " + Servidor.mapaint[pos1x][pos1y]);
                            if(Servidor.mapaint[pos1x][pos1y] == 4){ //se jogador tiver soltado bomba, deixa a imagem da bomba no lugar ao andar
                                Servidor.mapaint[pos1x][pos1y] = 6;
                            }
                            else if(Servidor.mapaint[pos1x][pos1y] == 2){//se não tiver soltado bomba
                                Servidor.mapaint[pos1x][pos1y] = 0;
                            }
                            pos1y++; //alterando a posição
                            Servidor.mapaint[pos1x][pos1y] = 2; //colocando jogador1 na nova posição
                            System.out.println("posicao nova[" + pos1x + "][" + pos1y + "] do jogador " + id + ": " +  Servidor.mapaint[pos1x][pos1y]);
                        }
                        else if (Servidor.mapaint[pos1x][pos1y + 1] == 7 || Servidor.mapaint[pos1x][pos1y + 1] == 8){//prox posição é explosão, mata o jogador
                            Servidor.mapaint[pos1x][pos1y] = 0;
                            vencedor = Servindo.placar(1); //chama função para atualizar o placar e reseta a posição do jogador1
                        }
                    }

                    if (aux == VK_W) { //se apertou W, move-se para cima
                        if (Servidor.mapaint[pos1x - 1][pos1y] == -1 || Servidor.mapaint[pos1x - 1][pos1y] == 1) {
                            System.out.println("nao pode ultrapassar bloco");
                        } else if (Servidor.mapaint[pos1x - 1][pos1y] == 0) { //prox posição é caminho, pode andar
                            System.out.println("posicao atual[" + pos1x + "][" + pos1y + "] do jogador " + id + ": " + Servidor.mapaint[pos1x][pos1y]);
                            if(Servidor.mapaint[pos1x][pos1y] == 4){ //se jogador tiver soltado bomba, deixa a imagem da bomba no lugar ao andar
                                Servidor.mapaint[pos1x][pos1y] = 6;
                            }
                            else if(Servidor.mapaint[pos1x][pos1y] == 2){ //se não tiver soltado bomba
                                Servidor.mapaint[pos1x][pos1y] = 0;
                            }
                            pos1x--; //alterando a posição
                            Servidor.mapaint[pos1x][pos1y] = 2; //colocando jogador1 na nova posição
                            System.out.println("posicao nova[" + pos1x + "][" + pos1y + "] do jogador " + id + ": " + Servidor.mapaint[pos1x][pos1y]);
                        }
                        else if (Servidor.mapaint[pos1x - 1][pos1y] == 7 || Servidor.mapaint[pos1x - 1][pos1y] == 8){//prox posição é explosão, mata o jogador
                            Servidor.mapaint[pos1x][pos1y] = 0;
                            vencedor = Servindo.placar(1); //chama função para atualizar o placar e reseta a posição do jogador1
                        }
                    }

                    if (aux == VK_S) { //se apertou W, move-se para cima
                        if (Servidor.mapaint[pos1x + 1][pos1y] == -1 || Servidor.mapaint[pos1x + 1][pos1y] == 1) {
                            System.out.println("nao pode ultrapassar bloco");
                        } else if (Servidor.mapaint[pos1x + 1][pos1y] == 0) { //prox posição é caminho, pode andar
                            System.out.println("posicao atual[" + pos1x + "][" + pos1y + "] do jogador " + id + ": " + Servidor.mapaint[pos1x][pos1y]);
                            if(Servidor.mapaint[pos1x][pos1y] == 4){ //se jogador tiver soltado bomba, deixa a imagem da bomba no lugar ao andar
                                Servidor.mapaint[pos1x][pos1y] = 6;
                            }
                            else if(Servidor.mapaint[pos1x][pos1y] == 2){ //se não tiver soltado bomba
                                Servidor.mapaint[pos1x][pos1y] = 0;
                            }
                            pos1x++; //alterando a posição
                            Servidor.mapaint[pos1x][pos1y] = 2; //colocando jogador1 na nova posição
                            System.out.println("posicao nova[" + pos1x + "][" + pos1y + "] do jogador " + id + ": " + Servidor.mapaint[pos1x][pos1y]);
                        }
                        else if (Servidor.mapaint[pos1x + 1][pos1y] == 7 || Servidor.mapaint[pos1x + 1][pos1y] == 8){ //prox posição é explosão, mata o jogador
                            Servidor.mapaint[pos1x][pos1y] = 0;
                            vencedor = Servindo.placar(1); //chama função para atualizar o placar e reseta a posição do jogador1
                        }
                    }
                    if (aux == VK_O) { //se apertou O, solta bomba na posição atual
                        if(jogador1_bomba == false) { //verifica se o jogador não soltou bomba
                            jogador1_bomba = true; //atribui true para mostrar que já soltou bomba
                            new Bomba(pos1x, pos1y, id).start(); //inicia thread da bomba
                        }
                    }
                }

                else if(id == 1) { //if que vai tratar a tecla mandada pelo JOGADOR 2 (mesmos procedimentos do JOGADOR 1)

                    if (aux == VK_A) {
                        if (Servidor.mapaint[pos2x][pos2y - 1] == -1 || Servidor.mapaint[pos2x][pos2y - 1] == 1) {
                            System.out.println("nao pode ultrapassar bloco");
                        } else if (Servidor.mapaint[pos2x][pos2y - 1] == 0) {
                            System.out.println("posicao atual[" + pos2x + "][" + pos2y + "] do jogador " + id + ": " + Servidor.mapaint[pos2x][pos2y]);
                            if(Servidor.mapaint[pos2x][pos2y] == 5){
                                Servidor.mapaint[pos2x][pos2y] = 6;
                            }
                            else if(Servidor.mapaint[pos2x][pos2y] == 3){
                                Servidor.mapaint[pos2x][pos2y] = 0;
                            }
                            pos2y--;
                            Servidor.mapaint[pos2x][pos2y] = 3;
                            System.out.println("posicao nova[" + pos2x + "][" + pos2y + "] do jogador " + id + ": " + Servidor.mapaint[pos2x][pos2y]);
                        }
                        else if (Servidor.mapaint[pos2x][pos2y - 1] == 7 || Servidor.mapaint[pos2x][pos2y - 1] == 8){
                            Servidor.mapaint[pos2x][pos2y] = 0;
                            vencedor = Servindo.placar(0);
                        }
                    }

                    if (aux == VK_D) {
                        if (Servidor.mapaint[pos2x][pos2y + 1] == -1 || Servidor.mapaint[pos2x][pos2y + 1] == 1) {
                            System.out.println("nao pode ultrapassar bloco");
                        } else if (Servidor.mapaint[pos2x][pos2y + 1] == 0) {
                            System.out.println("posicao atual[" + pos2x + "][" + pos2y + "] do jogador " + id + ": " + Servidor.mapaint[pos2x][pos2y]);
                            if(Servidor.mapaint[pos2x][pos2y] == 5){
                                Servidor.mapaint[pos2x][pos2y] = 6;
                            }
                            else if(Servidor.mapaint[pos2x][pos2y] == 3){
                                Servidor.mapaint[pos2x][pos2y] = 0;
                            }
                            pos2y++;
                            Servidor.mapaint[pos2x][pos2y] = 3;
                            System.out.println("posicao nova[" + pos2x + "][" + pos2y + "] do jogador " + id + ": " + Servidor.mapaint[pos2x][pos2y]);
                        }
                        else if (Servidor.mapaint[pos2x][pos2y + 1] == 7 || Servidor.mapaint[pos2x][pos2y + 1] == 8){
                            Servidor.mapaint[pos2x][pos2y] = 0;
                            vencedor = Servindo.placar(0);
                        }
                    }

                    if (aux == VK_W) {
                        if (Servidor.mapaint[pos2x - 1][pos2y] == -1 || Servidor.mapaint[pos2x - 1][pos2y] == 1) {
                            System.out.println("nao pode ultrapassar bloco");
                        } else if (Servidor.mapaint[pos2x - 1][pos2y] == 0) {
                            System.out.println("posicao atual[" + pos2x + "][" + pos2y + "] do jogador " + id + ": " + Servidor.mapaint[pos2x][pos2y]);
                            if(Servidor.mapaint[pos2x][pos2y] == 5){
                                Servidor.mapaint[pos2x][pos2y] = 6;
                            }
                            else if(Servidor.mapaint[pos2x][pos2y] == 3){
                                Servidor.mapaint[pos2x][pos2y] = 0;
                            }
                            pos2x--;
                            Servidor.mapaint[pos2x][pos2y] = 3;
                            System.out.println("posicao nova[" + pos2x + "][" + pos2y + "] do jogador " + id + ": " + Servidor.mapaint[pos2x][pos2y]);
                        }
                        else if (Servidor.mapaint[pos2x - 1][pos2y] == 7 || Servidor.mapaint[pos2x - 1][pos2y] == 8){
                            Servidor.mapaint[pos2x][pos2y] = 0;
                            vencedor = Servindo.placar(0);
                        }
                    }

                    if (aux == VK_S) {
                        if (Servidor.mapaint[pos2x + 1][pos2y] == -1 || Servidor.mapaint[pos2x + 1][pos2y] == 1) {
                            System.out.println("nao pode ultrapassar bloco");
                        } else if (Servidor.mapaint[pos2x + 1][pos2y] == 0) {
                            System.out.println("posicao atual[" + pos2x + "][" + pos2y + "] do jogador " + id + ": " + Servidor.mapaint[pos2x][pos2y]);
                            if(Servidor.mapaint[pos2x][pos2y] == 5){
                                Servidor.mapaint[pos2x][pos2y] = 6;
                            }
                            else if(Servidor.mapaint[pos2x][pos2y] == 3){
                                Servidor.mapaint[pos2x][pos2y] = 0;
                            }
                            pos2x++;
                            Servidor.mapaint[pos2x][pos2y] = 3;
                            System.out.println("posicao nova[" + pos2x + "][" + pos2y + "] do jogador " + id + ": " + Servidor.mapaint[pos2x][pos2y]);
                        }
                        else if (Servidor.mapaint[pos2x + 1][pos2y] == 7 || Servidor.mapaint[pos2x + 1][pos2y] == 8){
                            Servidor.mapaint[pos2x][pos2y] = 0;
                            vencedor = Servindo.placar(0);
                        }
                    }

                    if (aux == VK_O) {
                        if(jogador2_bomba == false) {
                            jogador2_bomba = true;
                            new Bomba(pos2x, pos2y, id).start();
                        }
                    }
                }

                envia(); //função que envia matriz atualizada para ps clientes
                System.out.println("aux: " + aux);

            }while (aux != 0); //fim de jogo

            for (int i=0; i<cont; i++)
                os[i].close();
            is.close();
            clientSocket.close(); //finaliza o Servidor

        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchElementException e) {
            System.out.println("Conexacao terminada pelo cliente");
            System.exit(1);
        }
    }

    static synchronized void envia(){
        //enviando a matriz atualizados pros clientes
        for(int i = 0; i < 2; i++) {
            for(int x = 0; x < 15; x++){
                for(int y = 0; y < 15; y++){
                    os[i].print(Servidor.mapaint[x][y] + " ");
                }
            }
            //envia a pontuação pros clientes
            os[i].print(pontos[0] + " " + pontos[1] + " ");
            os[i].print(tempo + " ");
            if(vencedor != 0){ //se o jogo acabou, envia 999 para terminar while do Cliente
                os[i].print(999 + " ");
            }
            else if(vencedor != J1VENCEU || vencedor != J2VENCEU)
                os[i].print(888 + " "); //manda qlq coisa para continuar o jogo
            os[i].flush();
        }

    }

    static int placar(int id){
        if(id == 0){ //jogador1 que vai ganhar ponto
            pontos[0]++; //incrementa no vetor de pontos
            pos2x = 13;
            pos2y = 13;
            Servidor.mapaint[pos2x][pos2y] = 3; //reseta a posição do jogador2
            if (pontos[0] >= VITORIA) {
                return J1VENCEU;
            }
        }
        else if(id == 1){ //jogador2 que vai ganhar ponto
            pontos[1]++; //incrementa no vetor de pontos
            pos1x = 1;
            pos1y = 1;
            Servidor.mapaint[pos1x][pos1y] = 2; //reseta posição do jogador1
            envia();
            if (pontos[1] >= VITORIA) {
                return J2VENCEU;
            }
        }
        return 0; //retorna 0 se não teve nenhum vencedor
    }
}

//----------------------------------------------Bomba------------------------------------------------
class Bomba extends Thread{
    int posX;
    int posY;
    int id;

    Bomba(int posX, int posY, int id) {
        this.posX = posX;
        this.posY = posY; //recebe posição em q a bomba foi plantada
        this.id = id; //recebe quem soltou a bomba

    }

    @Override
    public void run() {

        if(Servidor.mapaint[posX][posY] == 2) //se quem soltou foi o jogador1, atualiza a matriz com a imagem dele com a bomba
            Servidor.mapaint[posX][posY] = 4;


        else if(Servidor.mapaint[posX][posY] == 3) //se quem soltou foi o jogador2, atualiza a matriz com a imagem dele com a bomba
            Servidor.mapaint[posX][posY] = 5;

        Servindo.envia(); //envia matriz atualizada

        //esperando a bomba explodir
        try {
            Thread.sleep(2500);
        }
       catch (InterruptedException e){ }

        //switch para verificar a posição da bomba, explodir caso seja diferente de -1
        switch (Servidor.mapaint[posX][posY]){
            case -1:
                break;
            case 0:
                if(id == 0)
                    Servidor.mapaint[posX][posY] = 7;
                else if (id == 1)
                    Servidor.mapaint[posX][posY] = 8;
                break;
            case 1:
                if(id == 0)
                    Servidor.mapaint[posX][posY] = 7;
                else if (id == 1)
                    Servidor.mapaint[posX][posY] = 8;
                break;
            case 2:
                if(id == 0)
                    Servidor.mapaint[posX][posY] = 7;
                else if (id == 1)
                    Servidor.mapaint[posX][posY] = 8;
                Servindo.vencedor = Servindo.placar(1);
                break;   //mata o player 1
            case 3:
                if(id == 0)
                    Servidor.mapaint[posX][posY] = 7;
                else if (id == 1)
                    Servidor.mapaint[posX][posY] = 8;
                Servindo.vencedor = Servindo.placar(0);
                break;   //mata o player 2
            case 4:
                if(id == 0)
                    Servidor.mapaint[posX][posY] = 7;
                else if (id == 1)
                    Servidor.mapaint[posX][posY] = 8;
                Servindo.vencedor = Servindo.placar(1);
                break;   //mata o player 1 que esta em cima da bomba
            case 5:
                if(id == 0)
                    Servidor.mapaint[posX][posY] = 7;
                else if (id == 1)
                    Servidor.mapaint[posX][posY] = 8;
                Servindo.vencedor = Servindo.placar(0);
                break;   //mata o player 2 que esta em cima da bomba
            case 6:
                if(id == 0)
                    Servidor.mapaint[posX][posY] = 7;
                else if (id == 1)
                    Servidor.mapaint[posX][posY] = 8;
                break;   // trata como se fosse um quebravel (bomba)

        }

       //switch para verificar a posição em baixo da bomba
        switch (Servidor.mapaint[posX + 1][posY]){
            case -1:
                break;
            case 0:
                if(id == 0)
                    Servidor.mapaint[posX + 1][posY] = 7;
                else if (id == 1)
                    Servidor.mapaint[posX + 1][posY] = 8;
                break;
            case 1:
                if(id == 0)
                    Servidor.mapaint[posX + 1][posY] = 7;
                else if (id == 1)
                    Servidor.mapaint[posX + 1][posY] = 8;
                break;
            case 2:
                if(id == 0)
                    Servidor.mapaint[posX + 1][posY] = 7;
                else if (id == 1)
                    Servidor.mapaint[posX + 1][posY] = 8;
                Servindo.vencedor = Servindo.placar(1);
                break;   //mata o player 1
            case 3:
                if(id == 0)
                    Servidor.mapaint[posX + 1][posY] = 7;
                else if (id == 1)
                    Servidor.mapaint[posX + 1][posY] = 8;
                Servindo.vencedor = Servindo.placar(0);
                break;   //mata o player 2
            case 4:
                if(id == 0)
                    Servidor.mapaint[posX + 1][posY] = 7;
                else if (id == 1)
                    Servidor.mapaint[posX + 1][posY] = 8;
                Servindo.vencedor = Servindo.placar(1);
                break;   //mata o player 1 que esta em cima da bomba
            case 5:
                if(id == 0)
                    Servidor.mapaint[posX + 1][posY] = 7;
                else if (id == 1)
                    Servidor.mapaint[posX + 1][posY] = 8;
                Servindo.vencedor = Servindo.placar(0);
                break;   //mata o player 2 que esta em cima da bomba
            case 6:
                if(id == 0)
                    Servidor.mapaint[posX + 1][posY] = 7;
                else if (id == 1)
                    Servidor.mapaint[posX + 1][posY] = 8;
                break;   // trata como se fosse um quebravel (bomba)

        }

        //switch para verificar a posição em cima da bomba
        switch (Servidor.mapaint[posX - 1][posY]){
            case -1:
                break;
            case 0:
                if(id == 0)
                    Servidor.mapaint[posX - 1][posY] = 7;
                else if (id == 1)
                    Servidor.mapaint[posX - 1][posY] = 8;
                break;
            case 1:
                if(id == 0)
                    Servidor.mapaint[posX - 1][posY] = 7;
                else if (id == 1)
                    Servidor.mapaint[posX - 1][posY] = 8;
                break;
            case 2:
                if(id == 0)
                    Servidor.mapaint[posX - 1][posY] = 7;
                else if (id == 1)
                    Servidor.mapaint[posX - 1][posY] = 8;
                Servindo.vencedor = Servindo.placar(1);
                break;   //mata o player 1
            case 3:
                if(id == 0)
                    Servidor.mapaint[posX - 1][posY] = 7;
                else if (id == 1)
                    Servidor.mapaint[posX - 1][posY] = 8;
                Servindo.vencedor = Servindo.placar(0);
                break;   //mata o player 2
            case 4:
                if(id == 0)
                    Servidor.mapaint[posX - 1][posY] = 7;
                else if (id == 1)
                    Servidor.mapaint[posX - 1][posY] = 8;
                Servindo.vencedor = Servindo.placar(1);
                break;   //mata o player 1 que esta em cima da bomba
            case 5:
                if(id == 0)
                    Servidor.mapaint[posX - 1][posY] = 7;
                else if (id == 1)
                    Servidor.mapaint[posX - 1][posY] = 8;
                Servindo.vencedor = Servindo.placar(0);
                break;   //mata o player 2 que esta em cima da bomba
            case 6:
                if(id == 0)
                    Servidor.mapaint[posX - 1][posY] = 7;
                else if (id == 1)
                    Servidor.mapaint[posX - 1][posY] = 8;
                break;   // trata como se fosse um quebravel (bomba)

        }

        //switch para verificar a posição na esquerda da bomba
        switch (Servidor.mapaint[posX][posY - 1]){
            case -1:
                break;
            case 0:
                if(id == 0)
                    Servidor.mapaint[posX][posY - 1] = 7;
                else if (id == 1)
                    Servidor.mapaint[posX][posY - 1] = 8;
                break;
            case 1:
                if(id == 0)
                    Servidor.mapaint[posX][posY - 1] = 7;
                else if (id == 1)
                    Servidor.mapaint[posX][posY - 1] = 8;
                break;
            case 2:
                if(id == 0)
                    Servidor.mapaint[posX][posY - 1] = 7;
                else if (id == 1)
                    Servidor.mapaint[posX][posY - 1] = 8;
                Servindo.vencedor = Servindo.placar(1);
                break;   //mata o player 1
            case 3:
                if(id == 0)
                    Servidor.mapaint[posX][posY - 1] = 7;
                else if (id == 1)
                    Servidor.mapaint[posX][posY - 1] = 8;
                Servindo.vencedor = Servindo.placar(0);
                break;   //mata o player 2
            case 4:
                if(id == 0)
                    Servidor.mapaint[posX][posY - 1] = 7;
                else if (id == 1)
                    Servidor.mapaint[posX][posY - 1] = 8;
                Servindo.vencedor = Servindo.placar(1);
                break;   //mata o player 1 que esta em cima da bomba
            case 5:
                if(id == 0)
                    Servidor.mapaint[posX][posY - 1] = 7;
                else if (id == 1)
                    Servidor.mapaint[posX][posY - 1] = 8;
                Servindo.vencedor = Servindo.placar(0);
                break;   //mata o player 2 que esta em cima da bomba
            case 6:
                if(id == 0)
                    Servidor.mapaint[posX][posY - 1] = 7;
                else if (id == 1)
                    Servidor.mapaint[posX][posY - 1] = 8;
                break;   // trata como se fosse um quebravel (bomba)

        }

        //switch para verificar a posição na direita da bomba
        switch (Servidor.mapaint[posX][posY + 1]){
            case -1:
                break;
            case 0:
                if(id == 0)
                    Servidor.mapaint[posX][posY + 1] = 7;
                else if (id == 1)
                    Servidor.mapaint[posX][posY + 1] = 8;
                break;
            case 1:
                if(id == 0)
                    Servidor.mapaint[posX][posY + 1] = 7;
                else if (id == 1)
                    Servidor.mapaint[posX][posY + 1] = 8;
                break;
            case 2:
                if(id == 0)
                    Servidor.mapaint[posX][posY + 1] = 7;
                else if (id == 1)
                    Servidor.mapaint[posX][posY + 1] = 8;
                Servindo.vencedor = Servindo.placar(1);
                break;   //mata o player 1
            case 3:
                if(id == 0)
                    Servidor.mapaint[posX][posY + 1] = 7;
                else if (id == 1)
                    Servidor.mapaint[posX][posY + 1] = 8;
                Servindo.vencedor = Servindo.placar(0);
                break;   //mata o player 2
            case 4:
                if(id == 0)
                    Servidor.mapaint[posX][posY + 1] = 7;
                else if (id == 1)
                    Servidor.mapaint[posX][posY + 1] = 8;
                Servindo.vencedor = Servindo.placar(1);
                break;   //mata o player 1 que esta em cima da bomba
            case 5:
                if(id == 0)
                    Servidor.mapaint[posX][posY + 1] = 7;
                else if (id == 1)
                    Servidor.mapaint[posX][posY + 1] = 8;
                Servindo.vencedor = Servindo.placar(0);
                break;   //mata o player 2 que esta em cima da bomba
            case 6:
                if(id == 0)
                    Servidor.mapaint[posX][posY + 1] = 7;
                else if (id == 1)
                    Servidor.mapaint[posX][posY + 1] = 8;
                break;   // trata como se fosse um quebravel (bomba)

        }

        Servindo.envia(); //envia matriz atualizada

        //espera explosão da bomba acabar
        try {
            Thread.sleep(500);
        }
        catch (InterruptedException e){ }

        //terminando a explosão
        if(Servidor.mapaint[posX][posY] == 7 || Servidor.mapaint[posX][posY] == 8){
            Servidor.mapaint[posX][posY] = 0;
        }
        if(Servidor.mapaint[posX + 1][posY] == 7 || Servidor.mapaint[posX + 1][posY] == 8){
            Servidor.mapaint[posX + 1][posY] = 0;
        }
        if(Servidor.mapaint[posX - 1][posY] == 7 || Servidor.mapaint[posX - 1][posY] == 8){
            Servidor.mapaint[posX - 1][posY] = 0;
        }
        if(Servidor.mapaint[posX][posY + 1] == 7 || Servidor.mapaint[posX][posY + 1] == 8){
            Servidor.mapaint[posX][posY + 1] = 0;
        }
        if(Servidor.mapaint[posX][posY - 1] == 7 || Servidor.mapaint[posX][posY - 1] == 8){
            Servidor.mapaint[posX][posY - 1] = 0;
        }

        Servindo.envia(); //envia matriz atualizada de novo

        //atualiza variável booleana para que o jogador possa soltar bomba de novo
        if(id == 0)
            Servindo.jogador1_bomba = false;
        if(id == 1)
            Servindo.jogador2_bomba = false;
    }
}
