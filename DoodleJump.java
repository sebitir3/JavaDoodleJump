
import java.awt.Dimension;
import java.awt.Graphics;
import javax.swing.JComponent;
import javax.swing.JFrame;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.util.Random;
import java.util.Scanner;
import javax.imageio.ImageIO;
import javax.swing.Timer;

/**
 *
 * @author tiris0808
 */
public class DoodleJump extends JComponent implements ActionListener {

    // Height and Width of our game
    static final int WIDTH = 330;
    static final int HEIGHT = 600;

    //Title of the window
    String title = "Doodle Jump";

    // sets the framerate and delay for our game
    // this calculates the number of milliseconds per frame
    // you just need to select an approproate framerate
    int desiredFPS = 60;
    int desiredTime = Math.round((1000 / desiredFPS));

    // timer used to run the game loop
    // this is what keeps our time running smoothly :)
    Timer gameTimer;

    // YOUR GAME VARIABLES WOULD GO HERE
    Rectangle player = new Rectangle(120, 500, 60, 62);

    //BACKGROUND & COLORS
    int backgroundWidth = 8;
    int backgroundHeight = 8;
    Color paperLines = new Color(212, 208, 161);
    Color paperRedLine = new Color(212, 182, 167);
    Color platformGreen = new Color(110, 188, 34);
    Color scoreRectBlue = new Color(185, 203, 203, 170);
    Color powerupRed = new Color(207, 39, 78);

    //IMAGES
    //doodler image states
    BufferedImage DLeft;
    BufferedImage DRight;
    BufferedImage DShoot;

    //death states
    BufferedImage DLeftDead;
    BufferedImage DRightDead;

    //bat + animations
    BufferedImage Bat[] = new BufferedImage[3];
    BufferedImage Bat2;
    BufferedImage Bat3;

    //DOODLER STATES
    boolean moveRight = false;
    boolean moveLeft = false;
    boolean shoot = false;
    boolean shootWhileMove = false;
    boolean shootStanding = false;
    boolean startingDir = true;
    boolean lookLeft = false;
    boolean lookRight = false;
    //when true, doodler faces right
    //when false, doodler faces left
    boolean currentDirection = false;
    //used when colliding with 
    boolean collidesWith = true;
    //used to avoid bug when starting and shooting
    boolean shootAtStart = false;
    //if hits enemy
    boolean enemyHit = false;

    //JUMPING
    int moveSpeed = 5;
    int gravity = 1;
    int dy = 0;
    final int JUMP_POWER = -30;

    //SCORE
    Font scoreFont;
    int score;
    int posScore = score * -1;
    int allTimeHighscore;

    //GAMEOVER
    Font gameOverFont;
    Font gameOverSubFont;

    int gameOverTextX = 45;
    int highscoreX = 28;
    int gameOverTextY = 300;
    int textSpacer = 40;

    boolean newHighscore = false;

    //CAMERA
    int camY = 0;
    int camDiff;

    //DEATH
    boolean doodleDead = false;
    boolean doodleDeadDraw = false;
    int deathBoundary = camY + HEIGHT;
    int deathBoundaryExtended = deathBoundary + HEIGHT;

    //PLATFORMS
    int startPlatX = 115;
    int startPlatY = 550;

    int platRandX = 270;
    int platRandY = 200;

    Random rand = new Random();
    int platX = rand.nextInt(platRandX);
    int platY = rand.nextInt(platRandY);

    int platRepoHeight = 12000;

    int platHeight = 10;
    int platWidth = 50;
    int prevY = player.y;

    int numberOfPlatforms = 150;

    Rectangle platform[] = new Rectangle[numberOfPlatforms];

    //ENEMY
    int numberOfEnemies = 10;
    Rectangle batEnemy[] = new Rectangle[numberOfEnemies];

    int enemWidth = 120;
    int enemHeight = 67;
    int enemRandX = WIDTH - enemWidth;
    int startEnemX = 115;
    int startEnemY = -1000;
    int enemyRepoHeight = 1500;
    int camEnemyRepoHeight = enemyRepoHeight * numberOfEnemies + startEnemY;
    int enemX = rand.nextInt(enemRandX);
    
    //enemy movement
    int enemySpeed = 1;
    int[] enemyDir = new int[numberOfEnemies];
    
    //ANIMATION
    int animFrame = 0;
    int animDelay = 2;
    int delay = animDelay;

    //SHOOTING
    boolean bulletOnScreen = false;
    int bulletSpeed = 10;
    int bulletW = 7;
    int bulletH = 7;
    int numberOfBullets = 1;
    int bulletBoundary = player.y - HEIGHT;
    Rectangle bullet = new Rectangle();

    //POWERUPS
    int powerupInterval = 15;

    // GAME VARIABLES END HERE    
    public BufferedImage load(String filename) {
        BufferedImage image = null;
        //loading could cause errors
        try {
            //attempt image load
            image = ImageIO.read(new File(filename));
        } catch (Exception e) {
            //print out ugly error
            e.printStackTrace();
        }
        //send back image
        return image;
    }

    // Constructor to create the Frame and place the panel in
    // You will learn more about this in Grade 12 :)
    public DoodleJump() {
        // creates a windows to show my game
        JFrame frame = new JFrame(title);

        // sets the size of my game
        this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        // adds the game to the window
        frame.add(this);

        // sets some options and size of the window automatically
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        // shows the window to the user
        frame.setVisible(true);

        // add listeners for keyboard and mouse
        frame.addKeyListener(new Keyboard());
        Mouse m = new Mouse();
        this.addMouseMotionListener(m);
        this.addMouseWheelListener(m);
        this.addMouseListener(m);

        // Set things up for the game at startup
        setup();

        // Start the game loop
        gameTimer = new Timer(desiredTime, this);
        gameTimer.setRepeats(true);
        gameTimer.start();

        //importing true text file for different fonts
        //score
        try {
            scoreFont = Font.createFont(Font.TRUETYPE_FONT, new File("DoodleJump.ttf"));
            scoreFont = scoreFont.deriveFont(Font.BOLD, 30);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //game over large text
        try {
            gameOverFont = Font.createFont(Font.TRUETYPE_FONT, new File("DoodleJump.ttf"));
            gameOverFont = gameOverFont.deriveFont(Font.BOLD, 50);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //game over subtitle text
        try {
            gameOverSubFont = Font.createFont(Font.TRUETYPE_FONT, new File("DoodleJump.ttf"));
            gameOverSubFont = gameOverSubFont.deriveFont(Font.BOLD, 27);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //highscore
        try {
            //access text file
            Scanner scanner = new Scanner(new File("alltimeHighscore.txt"));
            //set number in file as highscore
            allTimeHighscore = scanner.nextInt();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    // drawing of the game happens in here
    // we use the Graphics object, g, to perform the drawing
    // NOTE: This is already double buffered!(helps with framerate/speed)
    @Override
    public void paintComponent(Graphics g) {
        // always clear the screen first!
        g.clearRect(0, 0, WIDTH, HEIGHT);

        // GAME DRAWING GOES HERE
        Graphics2D g2d = (Graphics2D) g;

        //paper backround
        //loop drawing rectangles horizontally, touching in a row
        for (int bgh = 0; bgh < WIDTH; bgh += backgroundWidth) {
            //regular rows with even heights
            for (int bgr = 0; bgr < HEIGHT; bgr += backgroundHeight) {
                if (bgr % 2 == 0) {
                    //set brick color to gray
                    g.setColor(paperLines);
                    g.drawRect(bgh, bgr, backgroundWidth, backgroundHeight);
                }
            }
        }
        //draw red line on paper background
        g.setColor(paperRedLine);
        g.drawRect(backgroundWidth * 3, 0, 1, HEIGHT);

        //draw red line on paper background
        g.setColor(paperRedLine);
        g.drawRect(backgroundWidth * 3, deathBoundaryExtended - 50, 1, HEIGHT);

        //drawing platforms
        for (int i = 0; i < numberOfPlatforms; i++) {
            //check for race condition error; platform might not have been created before being drawn
            if (platform[i] != null) {
                if (i % powerupInterval == 0 && i != 0) {
                    g.setColor(powerupRed);
                    g.fillRect(platform[i].x, platform[i].y - camY, platform[i].width, platform[i].height);
                } else {
                    //draw block
                    g.setColor(platformGreen);
                    g.fillRect(platform[i].x, platform[i].y - camY, platform[i].width, platform[i].height);
                }
            }
        }

        //draw bullets
        if (bulletOnScreen && doodleDead == false) {
            //draw bullet
            g.setColor(Color.GRAY);
            g.fillRect(bullet.x, bullet.y - camY, bullet.width, bullet.height);
        }

        //drawing enemies
        for (int i = 0; i < numberOfEnemies; i++) {
            if (doodleDead == false && batEnemy[i] != null) {
                g.drawImage(Bat[animFrame], batEnemy[i].x, batEnemy[i].y - camY, this);
            }
        }

        //draw game over screen when player dead
        if (doodleDead) {
            //draw game over! text
            g.setFont(gameOverFont);
            g.setColor(Color.RED);
            g.drawString("game over!", gameOverTextX, gameOverTextY);
            //draw game score text
            g.setFont(gameOverSubFont);
            g.setColor(Color.BLACK);
            g.drawString("your score: " + (score * - 1), gameOverTextX, gameOverTextY + textSpacer);
            //draw highscore textd
            g.setFont(gameOverSubFont);
            g.setColor(Color.BLACK);
            g.drawString("all-time highscore: " + allTimeHighscore, highscoreX, gameOverTextY + textSpacer * 2);
        }

        //drawing doodler
        if (startingDir) {
            lookRight = true;
            currentDirection = true;
        }

        //DRAWING IMAGES BASED ON MOVEMENT
        //if hit enemy and facing right --> draw upside down facing right 
        if (enemyHit && currentDirection) {
            g.drawImage(DRightDead, player.x, player.y - camY, this);
        } //if hit enemy and facing left --> draw upside down facing left 
        else if (enemyHit && currentDirection == false) {
            g.drawImage(DLeftDead, player.x, player.y - camY, this);
        } //if looking or moving right and current direction is rightward; draw right doodler && shooting while moving must not be occuring
        else if (((lookRight && currentDirection) || (moveRight && currentDirection)) && shootWhileMove == false) {
            g.drawImage(DRight, player.x, player.y - camY, this);
        } //if looking or moving left and current direction is leftward; draw left doodler && shooting while moving must not be occuring
        else if (((lookLeft && currentDirection == false) || (moveLeft && currentDirection == false)) && shootWhileMove == false) {
            g.drawImage(DLeft, player.x, player.y - camY, this);
        } //if shooting or moving while shooting; draw shooting doodler
        else if (shoot || shootWhileMove) {
            g.drawImage(DShoot, player.x, player.y - camY, this);
        }

        //draw score rectangle
        g.setColor(scoreRectBlue);
        g.fillRect(0, 0, WIDTH, 4 * backgroundHeight);

        //score text
        g.setFont(scoreFont);
        g.setColor(Color.BLACK);
        //draw point on screen as camY or score value (multiply by neg 1 since camY is a negative value)
        //when alive, draw camY value
        if (doodleDead == false) {
            g.drawString("" + (-1 * camY), 5, 25);
        } //when dead draw score value (set to camY before death)
        else if (doodleDead) {
            g.drawString("" + (-1 * score), 5, 25);
        }

        // GAME DRAWING ENDS HERE
    }

    // This method is used to do any pre-setup you might need to do
    // This is run before the game loop begins!
    public void setup() {
        // Any of your pre setup before the loop starts should go here

        //CALL TO LOAD IMAGES LOGIC
        loadImages();

        //CALL TO PLATFORM GENERATION LOGIC
        platformGen();

        //CALL TO ENEMY GENERATION LOGIC
        enemyGen();
        
        //ENEMY INIT MOVEMENT LOGIC
        initEnemyMovement();
    }

    public void loadImages() {
        //LOADING IN IMAGES
        //load in doodler
        DLeft = load("doodle//DLeft.png");
        DRight = load("doodle//DRight.png");
        DShoot = load("doodle//DShoot.png");

        DLeftDead = load("doodle//DLeftDead.png");
        DRightDead = load("doodle//DRightDead.png");

        //load enemies
        //running animation
        for (int i = 0; i < Bat.length; i++) {
            Bat[i] = load("doodle//EnemBat" + i + ".png");
        }
    }

    public void platformGen() {
        //PLATFORM GENERATION LOGIC
        for (int i = 0; i < numberOfPlatforms; i++) {
            //generate random x and y for platforms
            platX = rand.nextInt(platRandX);
            platY = rand.nextInt(platRandY);

            //starting platfrom
            if (i == 0) {
                platform[i] = new Rectangle(startPlatX, startPlatY, platWidth, platHeight);
            } //rest of the platforms with random x and y                        
            else {
                platform[i] = new Rectangle(platX, platform[i - 1].y - platY, platWidth, platHeight);
            }
        }
    }

    public void enemyGen() {
        //ENEMY GENERATION LOGIC
        for (int i = 0; i < numberOfEnemies; i++) {
            //generate random x and y for platforms
            enemX = rand.nextInt(enemRandX);

            //starting enemy
            if (i == 0 && doodleDead == false) {
                batEnemy[i] = new Rectangle(startEnemX, startEnemY, enemWidth, enemHeight);
            } //rest of enemies
            else if (doodleDead == false) {
                batEnemy[i] = new Rectangle(enemX, batEnemy[i - 1].y - enemyRepoHeight, enemWidth, enemHeight);
            }
        }
    }
    
    public void initEnemyMovement(){
        //ENEMY INIT MOVEMENT LOGIC
        for (int i = 0; i < numberOfEnemies; i++) {
            //move enemy left on startup
            enemyDir[i] = -1;
        }
    }

    // The main game loop
    // In here is where all the logic for my game will go
    public void loop() {
        //CALL TO DOODLER MOVEMENT LOGIC
        doodlerMovement();

        //CALL TO GRAVITY LOGIC
        gravity();

        //CALL TO CAMERA LOGIC
        doodlerCamera();

        //CALL TO BULLET GEN LOGIC
        bulletGen();
        
        //CALL TO EVENMY MOVEMENT LOGIC
        enemyMovement();

        //CALL TO PLAYER PLATFORM INTERSECTION LOGIC
        platformIntersection();

        //CALL TO BOUNDARY TELEPORTATION LOGIC
        sideTeleport();

        //CALL TO DEATH LOGIC
        deathLogic();

        //CALL TO ENEMY PLAYER INTERSECTION LOGIC
        enemyPlayerIntersection();

        //CALL TO BULLET ENEMY INTERSECTION LOGIC
        bulletEnemyIntersection();

        //CALL TO ENEMY ANIMATION LOGIC
        enemyAnim();

        //CALL TO HIGHSCORE LOGIC
        highscore();
    }

    public void doodlerMovement() {
        //DOODLER MOVEMENT LOGIC
        //disable movement upon hitting enemy
        if (enemyHit == false) {
            if (moveRight) {
                player.x += moveSpeed;
            } else if (moveLeft) {
                player.x -= moveSpeed;
            }
        }
    }

    public void gravity() {
        //GRAVITY LOGIC
        dy += gravity;
        player.y += dy / 2;
    }

    public void doodlerCamera() {
        //CAMERA FOLLOWING DOODLER LOGIC
        //follow middle of screen when player not dead
        if (doodleDead == false) {
            //track difference between one player height above player's y and the camera
            camDiff = (player.y - player.height) - camY;
            //if player passes a third of the height, move the cam up by the difference
            if (camDiff < HEIGHT / 3) {
                camY -= HEIGHT / 3 - camDiff;
            }
            //UPDATE DEATH BOUNDARY AND DEATH BOUNDARY EXTENDED
            deathBoundary = camY + HEIGHT;
            deathBoundaryExtended = deathBoundary + HEIGHT;
        }
    }

    public void enemyMovement() {
        //ENEMY MOVEMENT LOGIC
        for (int i = 0; i < numberOfEnemies; i++) {
            //bat movement
            
            batEnemy[i].x = batEnemy[i].x + enemyDir[i] * enemySpeed;

            // bounce off left or right (change direction)
            if (batEnemy[i].x < 0) {
                enemyDir[i] = 1;                
            } else if (batEnemy[i].x + batEnemy[i].width > WIDTH) {
                enemyDir[i] = -1;               
            }
        }
    }

    public void bulletGen() {
        //BULLET GENERATION LOGIC       
        //while bullet is on screen         
        if (bulletOnScreen) {
            bulletBoundary = player.y - HEIGHT;
            //move bullet up until passes player.y - height (allows for a delay to occur)
            if (bullet.y >= bulletBoundary) {
                bullet.y -= bulletSpeed;
            } //set bulletOnScreen to false when bullet passes player.y - height
            else if (bullet.y < bulletBoundary) {
                bulletOnScreen = false;
            }
        }
    }

    public void deathLogic() {
        //DEATH LOGIC
        if (player.y > deathBoundary) {
            //set doodler's state to dead
            doodleDead = true;

            //camera follows player at 1 fifth the height of the screen            
            camY = player.y + HEIGHT / 3;
            camY += dy;

            //when dead and before cameraY reaches finally death boundary
            if (camY > deathBoundaryExtended) {
                //freeze cameraY at deathBoundary
                camY = deathBoundaryExtended;
            }

        }

        //SCORE WHEN DEAD
        if (doodleDead == false) {
            //keep score as camY value before player dies
            score = camY;

            //if score in game is larger than highscore, set new highscore and allow to write in file
            if ((score * -1) > allTimeHighscore) {
                allTimeHighscore = (score * -1);
                newHighscore = true;
            }
        }
    }

    public void sideTeleport() {
        //BOUNDARY TELEPORTATION LOGIC
        //if moving left and less than x = 0, teleport to right side
        if (player.x + player.width < 0 && moveLeft) {
            player.x += WIDTH + player.width;
        } //if moving right and greater than screen width, teleport to left side
        else if (player.x > WIDTH && moveRight) {
            player.x = 0 - player.width;
        }
    }

    public void platformIntersection() {
        //if player is not hit by enemy
        if (enemyHit == false) {
            //PLAYER PLATFORM INTERSECTION LOGIC
            for (int i = 0; i < numberOfPlatforms; i++) {
                //intersection while falling
                if (player.intersects(platform[i]) && dy > 0) {
                    //fix up or down
                    //account for frame skipping with previous y
                    if ((player.y + player.height) >= platform[i].y && prevY <= platform[i].y) {
                        //now standing on platform
                        collidesWith = true;
                        //move the player up
                        player.y = platform[i].y - player.height;
                        //stop falling
                        dy = 0;

                        //powered up platforms (blue) every 15 platforms
                        if (i % powerupInterval == 0 && i != 0) {
                            //move doodler up by double jump power 
                            dy = JUMP_POWER * 2;
                            //set to false until collides with another platform
                            collidesWith = false;
                        }
                    }
                } //jump when doodler collides with a platform
                else if (collidesWith) {
                    //move doodler up by jump power
                    dy = JUMP_POWER;
                    //set to false until collides with another platform
                    collidesWith = false;

                } //PLATFORM CAMERA LOGIC
                //when bottom of camera passes y coord of platform; move platform up by the reposition height
                else if (doodleDead == false) {
                    if (platform[i].y > camY + HEIGHT) {
                        //if alive move to reposition height                
                        platform[i].y -= platRepoHeight;
                        platX = rand.nextInt(platRandX);
                    }
                } else if (doodleDead == false) {
                    if (batEnemy[i].y > camY + HEIGHT) {
                        //if alive move to reposition height                
                        batEnemy[i].y -= camEnemyRepoHeight;
                        enemX = rand.nextInt(platRandX);
                    }
                }
            }
            //track the previous y of the player for frame skipping collision errors
            prevY = player.y + player.height;
        }
    }

    public void enemyPlayerIntersection() {
        //PLAYER ENEMY INTERSECTION LOGIC
        for (int i = 0; i < numberOfEnemies; i++) {
            //intersection with enemy
            if (player.intersects(batEnemy[i])) {
                //set enemy hit true to allow for doodler to not intersect
                enemyHit = true;
            }
        }
    }

    public void bulletEnemyIntersection() {
        //BULLET ENEMY INTERSECTION LOGIC
        for (int i = 0; i < numberOfEnemies; i++) {
            //when bullet x or y is within bat enemy
            if (batEnemy[i].contains(bullet.x, bullet.y)) {
                //if alive move to reposition height                
                batEnemy[i].y -= camEnemyRepoHeight;
                enemX = rand.nextInt(platRandX);
            }
        }
    }
    
    public void enemyAnim(){
        //run anim
        if (doodleDead == false) {
            //increase frame
            delay--;
            if (delay == 0) {
                animFrame++;
                //start delay again
                delay = animDelay;
            }
            //reset when to big
            if (animFrame == Bat.length) {
                animFrame = 0;
            }
        }
    }

    public void highscore(){
        //try when new highscore set & dead
        if (newHighscore && doodleDead) {
            try {
                //acces text file
                FileWriter fw = new FileWriter("alltimeHighscore.txt");
                //write new highscore in file
                fw.write(String.valueOf(allTimeHighscore));
                //close file
                fw.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    
    // Used to implement any of the Mouse Actions
    private class Mouse extends MouseAdapter {

        // if a mouse button has been pressed down
        @Override
        public void mousePressed(MouseEvent e) {
        }

        // if a mouse button has been released
        @Override
        public void mouseReleased(MouseEvent e) {
        }

        // if the scroll wheel has been moved
        @Override
        public void mouseWheelMoved(MouseWheelEvent e) {
        }

        // if the mouse has moved positions
        @Override
        public void mouseMoved(MouseEvent e) {
        }
    }

    // Used to implements any of the Keyboard Actions
    private class Keyboard extends KeyAdapter {

        // if a key has been pressed down
        @Override
        public void keyPressed(KeyEvent e) {
            //get keycode
            int key = e.getKeyCode();
            //determine the buttonm
            if (key == KeyEvent.VK_D) {
                //when right pressed draw rightDoodler
                //move right
                moveRight = true;
                lookRight = false;

                //do not move or look left
                lookLeft = false;
                moveLeft = false;

                //do not shoot
                shoot = false;
                shootStanding = false;
                
                //set current direction to right
                currentDirection = true;

                //set starting dir to false after moving
                startingDir = false;
            }
            if (key == KeyEvent.VK_A) {
                //when left pressed draw leftDoodler
                //move left
                moveLeft = true;
                lookLeft = false;
                
                //do not move or look right
                moveRight = false;
                lookRight = false;

                //do not shoot
                shoot = false;
                shootStanding = false;
                
                //set current direction left
                currentDirection = false;

                //set starting dir to false after moving
                startingDir = false;
            }
            if (key == KeyEvent.VK_SPACE && !bulletOnScreen) {
                //when space pressed draw shootingDoodler               
                //set shooting true
                shoot = true;
                shootWhileMove = true;

                //not moving or looking left
                moveLeft = false;
                lookLeft = false;

                //not moving or looking right
                moveRight = false;
                lookRight = false;

                //create bullet
                bulletOnScreen = true;
                bullet = new Rectangle(player.x + player.width / 2, player.y, bulletW, bulletH);
                
                //if shooting at start, shoot and set starting direction false
                if (startingDir = true) {
                    shootAtStart = true;
                    startingDir = false;
                }
            }
        }

        // if a key has been released
        @Override
        public void keyReleased(KeyEvent e) {
            //get keycode
            int key = e.getKeyCode();
            //determine button
            if (key == KeyEvent.VK_D) {
                //when right released, keep drawing rightDoodler
                //check if not moving or looking left to make sure no stuttering occurs when right (d key) released
                if (lookLeft == false && moveLeft == false) {
                    //look right
                    moveRight = false;
                    lookRight = true;

                    //do not move or look left
                    lookLeft = false;
                    moveLeft = false;

                    //set current direction to right
                    currentDirection = true;
                }
                //set shoot false but allow to shoot standing
                shoot = false;
                shootStanding = true;

            }
            if (key == KeyEvent.VK_A) {
                //when left released, keep drawing leftDoodler
                //check if not moving or looking right to make sure no stuttering occurs when left (a key) released
                if (lookRight == false && moveRight == false) {
                    //do not move or look right
                    moveRight = false;
                    lookRight = false;

                    //look left
                    lookLeft = true;
                    moveLeft = false;

                    //set current direction left
                    currentDirection = false;
                }
                //set shoot false but allow to shoot standing
                shoot = false;
                shootStanding = true;

            }
            if (key == KeyEvent.VK_SPACE) {
                //when space released, doodler doesn't move or shoot
                //check if not moving and looking right and right to make sure no stuttering occurs when shoot (space bar) released

                if (lookRight == false && moveRight == false && lookLeft == false && moveRight == false) {                    
                    //disable shooting
                    shoot = false;
                    shootWhileMove = false;
                }

                //when space is released, set looking direction to the doodlers previous moveing/looking direction
                if (currentDirection == true) {
                    //look right
                    lookRight = true;
                    lookLeft = false;
                } else if (currentDirection == false) {
                    //look left
                    lookRight = false;
                    lookLeft = true;
                }
                //enable shoot standing and disable shoot while moving
                shootWhileMove = false;
                shootStanding = true;
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        loop();
        repaint();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // creates an instance of my game
        DoodleJump game = new DoodleJump();
    }
}
