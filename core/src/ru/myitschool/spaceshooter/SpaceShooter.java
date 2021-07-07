package ru.myitschool.spaceshooter;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;
import com.sun.org.apache.bcel.internal.generic.SWAP;

import java.util.ArrayList;

public class SpaceShooter extends ApplicationAdapter {
	public static final int SCR_WIDTH = 720, SCR_HEIGHT = 1280; // размеры экрана
	SpriteBatch batch; // объект для вывода изображений
	OrthographicCamera camera; // камера для масштабирования под все разрешения экранов
	Vector3 touch; // объект для определения касаний
	BitmapFont fontSmall; // объект для вывода текстов
	BitmapFont fontLarge;

	// текстуры и звуки
	Texture imgShip;
	Texture imgStars;
	Texture imgShoot;
	Texture imgEnemy;
	Texture imgTrash;
	Texture imgTrashShip;
	Texture imgSoundOn, imgSoundOff;
	Texture imgGyroSoundOn,imgGyroSoundOff;
	Sound sndShoot;
	Sound sndExplosion;
	boolean isSoundOn = false;
	boolean isGyroscopeOn = true;

	Ship ship; // наш корабль
	Space space[] = new Space[2]; // фон - звёздное небо
	Array<Shoot> shoots = new Array<>(); // выстрелы
	Array<Enemy> enemies = new Array<>(); // вражеские корабли
	Array<Trash> trashes = new Array<>(); // обломки вражеских кораблей
	Array<Trash> trashesShip = new Array<>(); // обломки нашего корабля

	long lastShootTime; // время последнего выстрела (через секунду после начала игры)
	long shootInterval = 400; // интервал между выстрелами
	long lastEnemyTime; // время появления последнего вражеского корабля
	long enemyInterval = 900; // интервал между появлениями вражеских кораблей

	int frags = 0; // количество убитых врагов
	static int level = 0; // уровень игры
	long timeDeathShip; // время, когда умер наш корабль
	long timeGameOver = 3000; // интервал между гибелью нашего корабля и появлением Game Over
	int stateOfGame; // статус игры
	static final int PLAYING = 0; // игра играет
	static final int WAIT_RESTART = 1; // ожидание рестарта
	static final int INTRO = 2; // ожидание рестарта

	Array<Player> players = new Array<>();
	int nPlayers = 10; // value of players in top

	SsButton buttonSound;
	SsButton buttonGyroscope;
	SsButton buttonPlay;
	SsButton buttonExit;
	SsButton buttonSettings;
	SsButton buttonCredits;
	float gX, gY; // приращения для гироскопа
	
	@Override
	public void create () {
		// создаём объекты
		batch = new SpriteBatch();
		camera = new OrthographicCamera();
		camera.setToOrtho(false, SCR_WIDTH, SCR_HEIGHT);
		touch = new Vector3();
		stateOfGame = INTRO;


		// загрузка картинок и звуков
		loadResources();
		fontGenerate();

		// создаём объекты кнопок
		buttonSound = new SsButton(SCR_WIDTH-70,SCR_HEIGHT-70,50,50);
		buttonGyroscope = new SsButton(SCR_WIDTH-70,SCR_HEIGHT-140,50,50);
		buttonPlay = new SsButton(SCR_WIDTH/2,SCR_HEIGHT-70,50,50);
		buttonExit = new SsButton(SCR_WIDTH/2,SCR_HEIGHT-90,50,50);
		buttonSettings = new SsButton(SCR_WIDTH/2,SCR_HEIGHT-110,50,50);
		buttonCredits = new SsButton(SCR_WIDTH/2,SCR_HEIGHT-130,50,50);

		// создаём массив игроков
		for(int i=0;i < nPlayers;i++){
			players.add(new Player("noname",0));
		}

		for(int i=0; i<2; i++) space[i]=new Space(); // создаём объекты неба
		space[1].y = SCR_HEIGHT; // один объект неба над другим

		ship = new Ship(); // создаём корабль
	}

	// метод, который постоянно повторяется в процессе игры
	@Override
	public void render () {
		if(stateOfGame == INTRO){
			if (Gdx.input.justTouched()) {
				touch.set(Gdx.input.getX(), Gdx.input.getY(), 0);
				camera.unproject(touch);
				stateOfGame = PLAYING;
			}

			batch.begin();
			fontLarge.draw(batch, "Space Shooter", 0 , SCR_HEIGHT - 20 - fontLarge.getCapHeight() / 2,
					SCR_WIDTH, Align.center, true);

			batch.end();
		}
		else{
			// обработка касаний экрана
			if (Gdx.input.justTouched()) {
				touch.set(Gdx.input.getX(), Gdx.input.getY(), 0);
				camera.unproject(touch);

				if (buttonSound.isTouched(touch.x, touch.y)) isSoundOn = !isSoundOn;
				if (buttonGyroscope.isTouched(touch.x, touch.y)) isGyroscopeOn = !isGyroscopeOn;
			}
			if (Gdx.input.isTouched()) {
				touch.set(Gdx.input.getX(), Gdx.input.getY(), 0);
				camera.unproject(touch);

				if (buttonSound.isTouched(touch.x, touch.y)) {
					isSoundOn = !isSoundOn;
				}
				if (!buttonGyroscope.isTouched(touch.x, touch.y) && !buttonSound.isTouched(touch.x, touch.y))
					ship.x += (touch.x - (ship.x + ship.width / 2)) / 20;

				if (stateOfGame == WAIT_RESTART)
					restart(); // если ждём рестарта и коснулись - рестарт
			}

			// обработка гироскопа
			if (Gdx.input.isPeripheralAvailable(Input.Peripheral.Gyroscope) && isGyroscopeOn) {
				float gyroX = Gdx.input.getGyroscopeX();
				float gyroY = Gdx.input.getGyroscopeY();
				// float gyroZ = Gdx.input.getGyroscopeZ();
				// gX+=gyroX;
				gY += gyroY;
				// применяем данные гироскопа, если не выходим за экран, чтобы не дёргался у края
				if (ship.x >= 0 && ship.x <= SCR_WIDTH - ship.width) ship.vx = gY;
			}

			// движение объектов неба
			for (int i = 0; i < space.length; i++) space[i].move();

			// порождение выстрелов, если корабль жив
			if (ship.isAlive)
				if (TimeUtils.millis() - lastShootTime > shootInterval) spawnShoot();

			// 	перемещение выстрелов
			for (int i = 0; i < shoots.size; i++) {
				shoots.get(i).move();
				// проверяем попадание выстрела в корабли врага
				for (int j = 0; j < enemies.size; j++) {
					if (shoots.get(i).overlaps(enemies.get(j))) {
						shoots.get(i).isAlive = false;
						enemies.get(j).isAlive = false;
						if (isSoundOn) sndExplosion.play();
						// порождается 100 обломков
						for (int k = 0; k < 100; k++) {
							trashes.add(new Trash(enemies.get(j)));
						}
						frags++;
						level = frags / 10 + 1; // уровень игры зависит от количества фрагов
					}
				}

				if (!shoots.get(i).isAlive)
					shoots.removeIndex(i); // удаляем из списка мёртвые выстрелы
			}

			// порождение вражеских кораблей
			if (TimeUtils.millis() - lastEnemyTime > enemyInterval) spawnEnemy();

			// перемещение вражеских кораблей
			for (int i = 0; i < enemies.size; i++) {
				enemies.get(i).move();
				if (enemies.get(i).y < 0 && ship.isAlive) gameOver(); // если прорвались за край
				if (!enemies.get(i).isAlive)
					enemies.removeIndex(i); // удаляем из списка мёртвых врагов
			}

			// перемещение обломков врагов
			for (int i = 0; i < trashes.size; i++) {
				trashes.get(i).move();
				if (!trashes.get(i).isAlive) trashes.removeIndex(i);
			}

			// перемещение обломков нашего корабля
			for (int i = 0; i < trashesShip.size; i++) {
				trashesShip.get(i).move();
				if (!trashesShip.get(i).isAlive) trashesShip.removeIndex(i);
			}

			// если прошло время с момента гибели корабля, переходим в режим ожидания рестарта
			if (!ship.isAlive && TimeUtils.millis() - timeDeathShip > timeGameOver) {
				stateOfGame = WAIT_RESTART;
			}

			// перемещение нашего корабля
			if (ship.isAlive) ship.move();

			camera.update(); // обновляем камеру
			batch.setProjectionMatrix(camera.combined); // пересчитываем размеры всех объектов

		/*Gdx.gl.glClearColor(1, 0, 0, 1); // очистка экрана не требуется - это делает небо
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);*/

			// вывод всех изображений
			batch.begin();
			for (int i = 0; i < space.length; i++)
				batch.draw(imgStars, space[i].x, space[i].y, space[i].width, space[i].height);

			for (int i = 0; i < trashes.size; i++)
				batch.draw(imgTrash, trashes.get(i).x, trashes.get(i).y,
						trashes.get(i).width / 2, trashes.get(i).height / 2,
						trashes.get(i).width, trashes.get(i).height,
						1, 1, trashes.get(i).aRotation,
						0, 0, 64, 64, false, false);

			for (int i = 0; i < trashesShip.size; i++)
				batch.draw(imgTrashShip, trashesShip.get(i).x, trashesShip.get(i).y,
						trashesShip.get(i).width / 2, trashesShip.get(i).height / 2,
						trashesShip.get(i).width, trashesShip.get(i).height,
						1, 1, trashesShip.get(i).aRotation,
						0, 0, 64, 64, false, false);

			for (int i = 0; i < shoots.size; i++)
				batch.draw(imgShoot, shoots.get(i).x, shoots.get(i).y);

			for (int i = 0; i < enemies.size; i++)
				batch.draw(imgEnemy, enemies.get(i).x, enemies.get(i).y);

			if (ship.isAlive)
				batch.draw(imgShip, ship.x, ship.y, ship.width, ship.height);

			fontSmall.draw(batch, "FRAGS: " + frags + " LEVEL: " + level,
					10, SCR_HEIGHT - fontSmall.getCapHeight());

			if (stateOfGame == WAIT_RESTART) {
				fontLarge.draw(batch, "GAME OVER", 0, SCR_HEIGHT / 2 + fontLarge.getCapHeight() / 2,
						SCR_WIDTH, Align.center, true);
				for (int i = 0; i < nPlayers; i++) {
					fontSmall.draw(batch, players.get(i).name + ": " + players.get(i).frags, 100, SCR_HEIGHT / 3 - i * 30);
				}
			}

			if (isSoundOn)
				batch.draw(imgSoundOn, buttonSound.x, buttonSound.y, buttonSound.width, buttonSound.height);
			else
				batch.draw(imgSoundOff, buttonSound.x, buttonSound.y, buttonSound.width, buttonSound.height);
			if (isGyroscopeOn)
				batch.draw(imgGyroSoundOn, buttonGyroscope.x, buttonGyroscope.y, buttonGyroscope.width, buttonGyroscope.height);
			else
				batch.draw(imgGyroSoundOff, buttonGyroscope.x, buttonGyroscope.y, buttonGyroscope.width, buttonGyroscope.height);

			batch.end();
		}
	}

	// загрузка картинок и звуков
	public void loadResources(){
		// загружаем текстуры
		imgShip = new Texture("ship.png");
		imgStars = new Texture("stars.png");
		imgShoot = new Texture("shoot.png");
		imgEnemy = new Texture("enemy.png");
		imgTrash = new Texture("part.png");
		imgTrashShip = new Texture("partship.png");
		imgSoundOn = new Texture("soundon.png");
		imgSoundOff = new Texture("soundoff.png");
		imgGyroSoundOn = new Texture("gyroon.png");
		imgGyroSoundOff = new Texture("gyrooff.png");

		// загружаем звуки
		sndShoot = Gdx.audio.newSound(Gdx.files.internal("blaster.wav"));
		sndExplosion = Gdx.audio.newSound(Gdx.files.internal("explosion.wav"));
	}

	// очистка памяти от картинок и звуков
	@Override
	public void dispose () {
		batch.dispose();
		imgShip.dispose();
		imgStars.dispose();
		imgShoot.dispose();
		imgTrash.dispose();
		imgTrashShip.dispose();
		imgGyroSoundOn.dispose();
		imgGyroSoundOff.dispose();
		sndShoot.dispose();
		sndExplosion.dispose();
	}

	// порождение выстрела
	void spawnShoot(){
		shoots.add(new Shoot(ship));
		lastShootTime = TimeUtils.millis();
		if(isSoundOn) sndShoot.play();
	}

	// порождение вражеского корабля
	void spawnEnemy(){
		enemies.add(new Enemy());
		lastEnemyTime = TimeUtils.millis();
	}

	// конец игры
	void gameOver(){
		ship.isAlive = false;
		if(isSoundOn) sndExplosion.play();
		// порождается 500 обломков
		for (int k=0; k<500; k++) {
			trashesShip.add(new Trash(ship));
		}

		timeDeathShip = TimeUtils.millis(); // фиксируем время гибели
		Preferences prefs = Gdx.app.getPreferences("preferences"); // заводим preferences

		for(int i = 0;i < nPlayers;i++){
			if(prefs.contains("Name"+i)) players.get(i).name = prefs.getString("Name"+i, "noname"); // читаем
			if(prefs.contains("Frags"+i)) players.get(i).frags = prefs.getInteger("Frags"+i, 0); // читаем
		}
		players.add(new Player("Paul",frags));

		for(int j = 0;j <nPlayers;j++) {
			for (int i = 0; i < nPlayers; i++) {
				if (players.get(i).frags < players.get(i + 1).frags) {
					int x = players.get(i).frags;
					String y = players.get(i).name;
					players.get(i).frags = players.get(i + 1).frags;
					players.get(i).name = players.get(i + 1).name;
					players.get(i + 1).frags = x;
					players.get(i + 1).name = y;
				}
			}
		}
		players.removeIndex(nPlayers);

		for(int i = 0;i < nPlayers;i++) {
			prefs.putString("Name"+i, players.get(i).name); // сохраняем число по ключу
			prefs.putInteger("Frags"+i, players.get(i).frags); // сохраняем число по ключу
		}
		prefs.flush(); // записываем

	}

	// рестарт игры
	void restart(){
		ship.isAlive=true;
		shoots.clear();
		enemies.clear();
		trashes.clear();
		trashesShip.clear();
		frags = 0;
		level = 0;
		//spawnShoot();
		//spawnEnemy();
		stateOfGame = PLAYING;
	}

	void fontGenerate(){
		FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("robotomonomedium.ttf"));
		FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
		final String FONT_CHARS = "абвгдеёжзийклмнопрстуфхцчшщъыьэюяabcdefghijklmnopqrstuvwxyzАБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789][_!$%#@|\\/?-+=()*&.;:,{}\"´`'<>";
		parameter.characters = FONT_CHARS;
		parameter.size = 30;
		parameter.color = Color.GREEN;
		fontSmall = generator.generateFont(parameter);
		parameter.size = 80;
		fontLarge = generator.generateFont(parameter);
		generator.dispose();
	}
}