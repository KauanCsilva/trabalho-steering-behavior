package cars.student;

import cars.engine.Car;
import cars.engine.Vector2;
import static cars.engine.Vector2.vec2;
import cars.engine.World;
import java.awt.*;


public class StudentCar extends Car {

private double anguloWander = 0;
private volatile int comportamento = 1;

private int pontoAtual = 0;

private Vector2[] caminho = {
    new Vector2(-250, -150),
    new Vector2(250, -150),
    new Vector2(250, 150),
    new Vector2(-250, 150)
};

public void setComportamento(int numero) {
        comportamento = numero;
    }

    public StudentCar() {
        super(settings ->
          settings
            .color(Color.BLUE)
            .randomOrientation()
        );
    }

    /**
     * Deve calcular o steering behavior para esse carro
     * O parametro world contem diversos metodos utilitários:
     * world.getClickPos(): Retorna um vector2D com a posição do último click,
     * ou nulo se nenhum click foi dado ainda
     * - world.getMousePos(): Retorna um vector2D com a posição do cursor do mouse
     * - world.getNeighbors(): Retorna os carros vizinhos. Não inclui o próprio carro.
     * Opcionalmente, você pode passar o raio da vizinhança. Se o raio não for
     * fornecido retornará os demais carros.
     * - world.getSecs(): Indica quantos segundos transcorreram desde o último quadro
     * Você ainda poderá chamar os seguintes metodos do carro para obter informações:
     * - getDirection(): Retorna um vetor unitário com a direção do veículo
     * - getPosition(): Retorna um vetor com a posição do carro
     * - getMass(): Retorna a massa do carro
     * - getMaxSpeed(): Retorna a velocidade de deslocamento maxima do carro em píxeis / s
     * - getMaxForce(): Retorna a forca maxima que pode ser aplicada sobre o carro
     */
   @Override
    public Vector2 calculateSteering(final World world) {
        Vector2 alvo = world.getClickPos();

       if (comportamento <= 3 && alvo == null) {
            return null;
        }

        Vector2 direcao = vec2();
        double velocidade = getMaxSpeed();

        switch (comportamento) {
            case 1: // Seek
                direcao = Vector2.subtract(alvo, getPosition());
                break;

            case 2: // Flee
                direcao = Vector2.subtract(getPosition(), alvo);
                break;

            case 3: // Arrive
                direcao = Vector2.subtract(alvo, getPosition());

                double distancia = direcao.size();
                double raioDesaceleracao = 200;

                velocidade *= Math.min(
                    distancia / raioDesaceleracao, 1.0
                );
                break;

            case 4: // Wander
                anguloWander += (Math.random() * 2 - 1)
                    * 4 * Math.sqrt(world.getSecs());

                Vector2 centro = getDirection().multiply(80);

                Vector2 deslocamento = getDirection()
                    .rotate(anguloWander)
                    .multiply(60);

                direcao = centro.add(deslocamento);
                break;

            
            case 5: // Path Following
                Vector2 ponto = caminho[pontoAtual];

                double distanciaPonto = Vector2.distance(
                    getPosition(), ponto
                );

                if (distanciaPonto < 30) {
                    pontoAtual++;

                    if (pontoAtual == caminho.length) {
                        pontoAtual = 0;
                    }

                    ponto = caminho[pontoAtual];
                }

                direcao = Vector2.subtract(ponto, getPosition());

                velocidade *= Math.min(direcao.size() / 200, 1.0);
    break;
        }

        Vector2 velocidadeDesejada = direcao.normalize()
            .multiply(velocidade);

        return Vector2.subtract(velocidadeDesejada, getVelocity());
    }

    public Vector2[] getCaminho() {
    return caminho;
    }
}
