import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.event.KeyEvent;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.Timer;
import javax.sound.sampled.*;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;

public class ClienteFrame extends JFrame implements Runnable {
    int tempo; //tempo de duração máxima do jogo
    Timer timer;
    static PrintStream os = null;
    JPanel menuPanel = new JPanel();
    JPanel infosPanel = new JPanel(new BorderLayout());
    JPanel RGPanel = new JPanel(new GridLayout(2,1));
    JPanel temporizadorPanel = new JPanel();
    JPanel botaoPanel = new JPanel(new GridLayout(2,1));
    MapaGui map = new MapaGui();

    JLabel Score1 = new JLabel("0");
    JLabel Score2 = new JLabel("0");

    JLabel tempoLabel = new JLabel("Tempo");
    JLabel timerLabel = new JLabel("300");
    JLabel playersLabel = new JLabel("Jogadores");
    JButton fecharBotao = new JButton("Fechar");

    Interface Rg1 = new Interface(1);
    Interface Rg2 = new Interface(2);
    static int[] pontos = new int[2];
    public static int[][] Mapainteiro = new int[][]{ //matriz de inteiros do servidor
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
    ClienteFrame() {
        super("Super Bomberman - TP");
        RGPanel.add(Rg1);
        Rg1.add(Score1);
        Rg2.add(Score2);
        RGPanel.add(Rg2);
        menuPanel.setLayout(new BorderLayout());
        temporizadorPanel.setLayout(new BorderLayout());
        infosPanel.add(RGPanel, BorderLayout.CENTER);
        this.add(menuPanel, BorderLayout.WEST);
        this.add(infosPanel, BorderLayout.EAST);
        this.add(map);

        infosPanel.add(playersLabel, BorderLayout.NORTH);
        botaoPanel.add(fecharBotao);
        temporizadorPanel.add(tempoLabel, BorderLayout.NORTH);
        temporizadorPanel.add(timerLabel, BorderLayout.SOUTH);
        menuPanel.add(botaoPanel, BorderLayout.SOUTH);
        menuPanel.add(temporizadorPanel, BorderLayout.NORTH);
        menuPanel.setBorder(BorderFactory.createTitledBorder(" "));

        menuPanel.setFocusable(false);
        infosPanel.setFocusable(false);
        temporizadorPanel.setFocusable(false);
        botaoPanel.setFocusable(false);
        fecharBotao.setFocusable(false);
        fecharBotao.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(1);
            }
        });

        this.setSize(770,630);
        setResizable(false);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);

        this.addKeyListener(new KeyListener(){
            public void keyTyped (KeyEvent e){}

            public void keyPressed (KeyEvent e){//recebe a tecla do teclado


                if (e.getKeyCode() == KeyEvent.VK_W){
                    os.println(KeyEvent.VK_W);
                    System.out.println("tecla pressionada: w");
                }

                if (e.getKeyCode() == KeyEvent.VK_A){
                    os.println(KeyEvent.VK_A);
                    System.out.println("tecla pressionada: a");
                }

                if (e.getKeyCode() == KeyEvent.VK_S){
                    os.println(KeyEvent.VK_S);
                    System.out.println("tecla pressionada: s");
                }

                if (e.getKeyCode() == KeyEvent.VK_D){
                    os.println(KeyEvent.VK_D);
                    System.out.println("tecla pressionada: d");
                }

                if (e.getKeyCode() == KeyEvent.VK_O){
                    os.println(KeyEvent.VK_O);
                    System.out.println("tecla pressionada: o");
                }
            }

            public void keyReleased (KeyEvent e){}
        });
    }

    public static void main(String[] args) {
        new Thread(new ClienteFrame()).start();
        try {
            while (true) {
                playSound("sound/level-3-arranged.mid");
                Thread.sleep(90000);
                //sleep para musica de 1min 29s
            }
        } catch (InterruptedException e) {
        }
    }

    private static synchronized void playSound(final String arq) {
        try {
            AudioInputStream ais = AudioSystem.getAudioInputStream(new File(arq));
            Clip c = AudioSystem.getClip(AudioSystem.getMixerInfo()[1]);
            c.open(ais);
            c.start();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void run() {
        Socket socket = null;
        Scanner is = null;

        try {
            socket = new Socket("127.0.0.1", 80);
            os = new PrintStream(socket.getOutputStream(), true);
            is = new Scanner(socket.getInputStream());
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host.");
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to host");
        }

        try {
            do {
                //recebendo e printando a matriz de inteiros atualizada
                for(int x = 0; x < 15; x++) {
                    for (int y = 0; y < 15; y++) {
                        Mapainteiro[x][y] = is.nextInt();
                        System.out.print(" " + Mapainteiro[x][y] + " ");
                    }
                    System.out.print("\n");
                }

                System.out.print("\n");
                for (int i = 0; i < 2; i++){
                    pontos[i] = is.nextInt();
                }

                tempo = is.nextInt();
                if (tempo == 0){
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            fim_tempo();
                        }
                    });
                }
                Runnable doWorkRunnable = new Runnable() { //atualiza a Label dos pontos
                    public void run() {
                        mudaLabels(pontos[0], pontos[1], tempo);
                    }
                };
                SwingUtilities.invokeLater(doWorkRunnable); //chama a função para atualizar a Label da Thread da Swing

                map.repaint(); //redesenha o mapa com as atualizações do servidor
            } while (is.nextInt() != 999); //fim de jogo, houve um vencedor

            //os.print(0); //manda zero para parar o while do servidor
            System.out.println("Fim de jogo");
            Runnable doWorkRunnable2 = new Runnable() { //chama função para abrir uma janela de fim de jogo
                public void run() {
                    fim_mensagem(pontos[0], pontos[1]);
                }
            };
            SwingUtilities.invokeLater(doWorkRunnable2); //faz na Thread da Swing

            os.close();
            is.close();
            socket.close(); //finaliza o cliente
        } catch (UnknownHostException e) {
            System.err.println("Trying to connect to unknown host: " + e);
        } catch (IOException e) {
            System.err.println("IOException:  " + e);
        }
    }

    void mudaLabels(int ponto1, int ponto2, int tempo){ //função para atulizar Label do placar
        Score1.setText(""+ponto1);
        Score2.setText(""+ponto2);
        timerLabel.setText(""+tempo);
    }

    void fim_mensagem(int ponto1, int ponto2){ //exibe a janela de fim de jogo
        if(ponto1 >= 2){ //verfica quem foi o vencedor
            JOptionPane.showMessageDialog(this, "Jogador 1 venceu", "FIM DE JOGO", JOptionPane.INFORMATION_MESSAGE);
            System.exit(1); //fecha o cliente
        }
        else if(ponto2 >= 2){ //mesma procedimento caso o vencedor seja o jogador2
            JOptionPane.showMessageDialog(this, "Jogador 2 venceu", "FIM DE JOGO", JOptionPane.INFORMATION_MESSAGE);
            System.exit(1);
        }
    }

    void fim_tempo(){
        JOptionPane.showMessageDialog(ClienteFrame.this, "TEMPO ACABOU", "Empate", JOptionPane.ERROR_MESSAGE);
        System.exit(1);
    }
}

//-----------------------------------------------------MapaGui---------------------------------------------------
class MapaGui extends JPanel{
    Image[] img = new Image[10]; // Vetor de Imagens
    //Constantes para indicar qual imagem sera representada pelo indice do vetor
    final int PATH = 0;
    final int UNBREAK = 1;
    final int BREAK = 2;
    final int BOMBERMAN = 3;
    final int BOMBERMAN2 = 4;
    final int BOMB = 5;
    final int BOMBERMAN_BOMBA = 6;
    final int BOMBERMAN2_BOMBA = 7;
    final int EXPLODE = 8;
    final int EXPLODEB = 9;

    MapaGui(){
        try {
            // Carrega a imagem no indice indicado (pela constante) do vetor de Imagens
            img[PATH]   = ImageIO.read(new File("images/caminho.png"));
            img[UNBREAK]  = ImageIO.read(new File("images/18.gif"));
            img[BREAK]   = ImageIO.read(new File("images/breakable.jpg"));
            img[BOMBERMAN]   = ImageIO.read(new File("images/bomber.gif"));
            img[BOMBERMAN2]   = ImageIO.read(new File("images/bomber2.gif"));
            img[BOMBERMAN_BOMBA]   = ImageIO.read(new File("images/bomberBomba2.gif"));
            img[BOMBERMAN2_BOMBA]   = ImageIO.read(new File("images/bomber2Bomba2.gif"));
            img[BOMB]   = ImageIO.read(new File("images/bomba.gif"));
            img[EXPLODE]   = ImageIO.read(new File("images/explosao.gif"));
            img[EXPLODEB]   = ImageIO.read(new File("images/explosaoazul.gif"));
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "A imagem nao pode ser carregada!\n" + e, "Erro", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    public void paintComponent(Graphics g) { // Funcao responsavel por desenhar o mapa e seus componentes, dado mapa de inteiros
        super.paintComponent(g);
        for(int y = 0; y < 15; y++){
            for(int x = 0; x < 15; x++){
                if(ClienteFrame.Mapainteiro [y][x] == -1){
                    g.drawImage(img[UNBREAK], x*40, y*40, 40,40, this);
                }
                else if(ClienteFrame.Mapainteiro[y][x] == 0){
                    g.drawImage(img[PATH], x*40, y*40, 40,40, this);
                }
                else if(ClienteFrame.Mapainteiro[y][x] == 1){
                    g.drawImage(img[BREAK], x*40, y*40, 40,40, this);
                }
                else if(ClienteFrame.Mapainteiro[y][x] == 2){
                    g.drawImage(img[BOMBERMAN], x*40, y*40, 40,40, this);
                }
                else if(ClienteFrame.Mapainteiro[y][x] == 3){
                    g.drawImage(img[BOMBERMAN2], x*40, y*40, 40,40, this);
                }
                else if(ClienteFrame.Mapainteiro[y][x] == 4){
                    g.drawImage(img[BOMBERMAN_BOMBA], x*40, y*40, 40,40, this);
                }
                else if(ClienteFrame.Mapainteiro[y][x] == 5){
                    g.drawImage(img[BOMBERMAN2_BOMBA], x*40, y*40, 40,40, this);
                }
                else if(ClienteFrame.Mapainteiro[y][x] == 6){
                    g.drawImage(img[BOMB], x*40, y*40, 40,40, this);
                }
                else if(ClienteFrame.Mapainteiro[y][x] == 7){
                    g.drawImage(img[EXPLODE], x*40, y*40, 40,40, this);
                }
                else if(ClienteFrame.Mapainteiro[y][x] == 8){
                    g.drawImage(img[EXPLODEB], x*40, y*40, 40,40, this);
                }
            }
        }
        Toolkit.getDefaultToolkit().sync();
    }
}

//----------------------------------------------------Interface------------------------------------------------------------
class Interface extends JPanel {
    Image[] img = new Image[5];
    final int RG1 = 0; // Imagem do Jogador 1 no painel de Jogadores
    final int RG2 = 1; // Imagem do Jogador 2 no painel de Jogadores
    int Escolha; // Variavel criada para a escolher a instancia correta

Interface(int Escolha) { // Parametro do construtor ira imagem sera desenhada
    this.Escolha = Escolha;
  this.setBorder(BorderFactory.createTitledBorder("Pontos"));
    this.setSize(100,200);
    try {
        if(Escolha == 1) // Imagem do Jogador 1
            img[RG1] = ImageIO.read(new File("images/player1RG.png"));
        else if(Escolha == 2)// Imagem do Jogador 2
            img[RG2] = ImageIO.read(new File("images/player2RG.png"));
    } catch (IOException e) {
        JOptionPane.showMessageDialog(this, "A imagem nao pode ser carregada!\n" + e, "Erro", JOptionPane.ERROR_MESSAGE);
        System.exit(1);
    }

}
    public void paintComponent(Graphics g){ // Funcao responsavel por desenhar as imagens do painel Jogadores
        super.paintComponent(g);
        g.drawImage(img[RG1], 10, 45, 40,40, this);
        g.drawImage(img[RG2],10, 45, 40,40, this);
        Toolkit.getDefaultToolkit().sync();
    }
}