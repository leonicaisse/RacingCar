package fr.iutlens.mmi.racingcar;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

import fr.iutlens.mmi.racingcar.utils.OrientationProxy;
import fr.iutlens.mmi.racingcar.utils.RefreshHandler;
import fr.iutlens.mmi.racingcar.utils.SpriteSheet;
import fr.iutlens.mmi.racingcar.utils.TimerAction;

public class GameView extends View implements TimerAction,  OrientationProxy.OrientationListener {
    private Track track;
    private Car car;
    private RefreshHandler timer;

    public GameView(Context context) {
        super(context);
        init(null, 0);
    }

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public GameView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    /**
     * Initialisation de la vue
     *
     * Tous les constructeurs (au-dessus) renvoient ici.
     *
     * @param attrs
     * @param defStyle
     */
    private void init(AttributeSet attrs, int defStyle) {

        // Chargement des feuilles de sprites
        SpriteSheet.register(R.drawable.design,3,6,this.getContext());
        SpriteSheet.register(R.drawable.kayakset,4,4,this.getContext());

        // Création des différents éléments à afficher dans la vue
        track = new Track(null,R.drawable.design);
        car = new Car(R.drawable.kayakset,3,8);

        // Gestion du rafraichissement de la vue. La méthode update (juste en dessous)
        // sera appelée toutes les 30 ms
        timer = new RefreshHandler(this);

        // Un clic sur la vue lance (ou relance) l'animation
        this.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!timer.isRunning()) timer.scheduleRefresh(30);
            }
        });
    }

    /**
     * Mise à jour (faite toutes les 30 ms)
     */
    @Override
    public void update() {
        if (this.isShown()) { // Si la vue est visible
            timer.scheduleRefresh(30); // programme le prochain rafraichissement
            car.update(track); // mise à jour de la position de la voiture

            if (car.x> track.getSizeX()) car.x -= track.getSizeX();
            if (car.x<0) car.x += track.getSizeX();
            if (car.y> track.getSizeX()) car.y -= track.getSizeY();
            if (car.y<0) car.y += track.getSizeY();


            invalidate(); // demande à rafraichir la vue
        }
    }

    /**
     * Méthode appelée (automatiquement) pour afficher la vue
     * C'est là que l'on dessine le décor et les sprites
     * @param canvas
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // On met une couleur de fond
        canvas.drawColor(0xff000000);

        // On choisit la transformation à appliquer à la vue i.e. la position
        // de la "camera"
        setCamera(canvas);

        // Dessin des différents éléments
        track.paint(canvas);
        car.paint(canvas,track.getTileWidth(),track.getTileHeight());

    }

    private void setCamera(Canvas canvas) {

        // On calcul le facteur de zoom nécessaire pour afficher au moins 7 tiles
        // dans chaque direction
        float tiles_x = (1.0f*getWidth())/track.getTileWidth();
        float tiles_y =  (1.0f*getHeight())/track.getTileHeight();
        float min_tiles = Math.min(tiles_x,tiles_y);
        float scale = (min_tiles)/12;

        // La suite de transfomations est à interpréter "à l'envers"

        // On termine par un centrage de l'origine (la voiture donc) dans la fenêtre
        canvas.translate(getWidth()/2,getHeight()/2);

        // On tourne le tout dans le sens inverse à l'angle de la voiture par rapport à la pise
        // Du coup, la voiture sera toujours orientée pareil à l'écran, c'est le décor qui bougera
//        canvas.rotate(-car.direction);

        // On mets à l'échelle calculée au dessus
        canvas.scale(scale, scale);

        // On centre sur la position actuelle de la voiture (qui se retrouve en 0,0 )
        canvas.translate(-track.getWidth()/2,-car.y *track.getTileHeight());
    }


    @Override
    public void onOrientationChanged(float[] angle, long stamp) {
        car.setCommand(Math.toDegrees(angle[1]),Math.toDegrees(angle[2]));

    }
}
