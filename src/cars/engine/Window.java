package cars.engine;

import cars.student.Setup;
import cars.student.StudentCar;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import javax.swing.*;

public final class Window extends JFrame implements Runnable {
    public static final int INITIAL_WIDTH = 1024;
    public static final int INITIAL_HEIGHT = 768;

    private final List<Car> cars;
    private Vector2 clickPos = null;
    private Vector2 mousePos = null;
    private boolean debugMode = true;

    private Window() {
        super("Steering behaviors");

        setSize(INITIAL_WIDTH, INITIAL_HEIGHT);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setIgnoreRepaint(true);
        setResizable(true);

        this.cars = new Setup().createCars();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                requestFocusInWindow();
                start();
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                clickPos = new Vector2(e.getX() - getWidth() / 2.0, e.getY() - getHeight() / 2.0);

                requestFocusInWindow();
            }
        });

        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                mousePos = new Vector2(e.getX() - getWidth() / 2.0, e.getY() - getHeight() / 2.0);
            }
        });

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                StudentCar carro = (StudentCar) cars.get(0);

                switch (e.getKeyChar()) {
                    case '1':
                        carro.setComportamento(1);
                        break;

                    case '2':
                        carro.setComportamento(2);
                        break;

                    case '3':
                        carro.setComportamento(3);
                        break;

                    case '4':
                        carro.setComportamento(4);
                        break;

                    case '5':
                        carro.setComportamento(5);
                        break;

                    case '6':
                        carro.setComportamento(6);
                        break;

                    case 'd':
                    case 'D':
                        debugMode = !debugMode;
                        break;
                }
            }
        });
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> new Window().setVisible(true));
    }

    private void start() {
        createBufferStrategy(3);

        final var gameLoop = new Thread(this, "game-loop");
        gameLoop.setDaemon(true);
        gameLoop.start();
    }

    @Override
    public void run() {
        double prev = System.currentTimeMillis() - 1;

        try {
            final var strategy = getBufferStrategy();

            while (true) {
                final double now = System.currentTimeMillis();
                final var g2d = (Graphics2D) strategy.getDrawGraphics();

                draw(g2d);
                update((now - prev) / 1000.0);

                g2d.dispose();
                Toolkit.getDefaultToolkit().sync();

                prev = now;
                strategy.show();

                Thread.sleep(1);
            }
        } catch (InterruptedException e) {
            System.err.println("Interrupted");
        }

        System.exit(0);
    }

    private void update(final double secs) {
        cars.forEach(car -> car.update(new World(secs, car, cars, mousePos, clickPos, getWidth(), getHeight())));
    }

    private void draw(Graphics2D g2d) {
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        g2d.setBackground(new Color(220, 220, 220));
        g2d.clearRect(0, 0, getWidth(), getHeight());

        g2d.translate(getWidth() / 2.0, getHeight() / 2.0);

        StudentCar carro = (StudentCar) cars.get(0);

        // Desenha o caminho do carro
        Vector2[] caminho = carro.getCaminho();

        g2d.setColor(Color.GRAY);

        for (int i = 0; i < caminho.length; i++) {
            Vector2 ponto = caminho[i];

            int proximo = i + 1;

            if (proximo == caminho.length) {
                proximo = 0;
            }

            Vector2 pontoSeguinte = caminho[proximo];

            g2d.drawLine((int) ponto.x, (int) ponto.y, (int) pontoSeguinte.x, (int) pontoSeguinte.y);
        }

        g2d.setColor(Color.RED);

        for (int i = 0; i < caminho.length; i++) {
            Vector2 ponto = caminho[i];

            g2d.fillOval((int) ponto.x - 5, (int) ponto.y - 5, 10, 10);
            g2d.drawString("" + (i + 1), (int) ponto.x + 10, (int) ponto.y);
        }

        // Desenha o obstáculo do carro
        Vector2 obstaculo = carro.getObstaculo();
        int raio = carro.getRaioObstaculo();

        g2d.setColor(Color.ORANGE);
        g2d.fillOval((int) obstaculo.x - raio, (int) obstaculo.y - raio, raio * 2, raio * 2);

        if (clickPos != null) {
            g2d.setColor(Color.GRAY);
            g2d.fillOval((int) clickPos.x - 4, (int) clickPos.y - 4, 8, 8);
        }

        cars.forEach(car -> car.draw(g2d, debugMode));

        g2d.setFont(new Font("Arial", Font.PLAIN, 12));
        g2d.setColor(Color.DARK_GRAY);

        g2d.drawString("1 Seek | 2 Flee | 3 Arrive | 4 Wander | 5 Caminho + Desvio | 6 Wander + Desvio | D Vetores", 20.0f - getWidth() / 2.0f, getHeight() / 2.0f - 20.0f);
    }
}