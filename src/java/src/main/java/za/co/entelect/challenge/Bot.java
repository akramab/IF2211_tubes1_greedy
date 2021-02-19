package za.co.entelect.challenge;

import za.co.entelect.challenge.command.*;
import za.co.entelect.challenge.entities.*;
import za.co.entelect.challenge.enums.CellType;
import za.co.entelect.challenge.enums.Direction;

import java.util.*;
import java.util.stream.Collectors;


public class Bot {

    private Random random;
    private GameState gameState;
    private Opponent opponent;
    private MyWorm currentWorm;

    public Bot(Random random, GameState gameState) {
        this.random = random;
        this.gameState = gameState;
        this.opponent = gameState.opponents[0];
        this.currentWorm = getCurrentWorm(gameState);

    }

    private MyWorm getCurrentWorm(GameState gameState) {
        return Arrays.stream(gameState.myPlayer.worms)
                .filter(myWorm -> myWorm.id == gameState.currentWormId)
                .findFirst()
                .get();
    }

    public Command run() {
        //PRIORITAS 1: SPECIAL ATTACK (Snowball/Banana Bomb)
        Worm enemyWormSpecial = getFirstWormInRangeSpecial();
        if (enemyWormSpecial != null) {
            if (gameState.myPlayer.worms[1].id == gameState.currentWormId) {
                if (gameState.myPlayer.worms[1].bananaBombs.count > 0) {
                    return new BananaBombCommand(enemyWormSpecial.position.x, enemyWormSpecial.position.y);
                }
            }
            if (gameState.myPlayer.worms[2].snowballs.count > 0) {
                return new SnowballCommand(enemyWormSpecial.position.x, enemyWormSpecial.position.y);
            }
        }

        //PRIORITAS 2: NORMAL ATTACK (Shoot)
        Worm enemyWorm = getFirstWormInRange();
        if (enemyWorm != null) {
            Direction direction = resolveDirection(currentWorm.position, enemyWorm.position);
            return new ShootCommand(direction);
        }

        //PRIORITAS 3: MOVE (Karena sudah opsi serang tidak memungkinkan)

        //Ambil semua koordinat di cell Worm saat ini selain current cell
        List<Cell> surroundingBlocks = getSurroundingCells(currentWorm.position.x, currentWorm.position.y);
        

        //Kalau Worm saat ini adalah Commander, masuk ke algoritma move khusus Commander
        if(gameState.currentWormId == 1){

            //Commander akan memprioritaskan untuk bergerak menuju Technician (Worm yang memiliki Snowball)
            Worm technicianWorm = gameState.myPlayer.worms[2];


            //Mencari cell yang akan menghasilkan jarak terpendek menuju Worm tujuan
            Cell shortestCellToTechnician = getShortestPath(surroundingBlocks, technicianWorm.position.x, technicianWorm.position.y);
        
            //Commander akan bergerak mendekati Technician sampai dengan jarak dia dan Technician paling dekat adalah 3 satuan
            if (euclideanDistance(currentWorm.position.x, currentWorm.position.y, technicianWorm.position.x, technicianWorm.position.y) > 3) {
                if(shortestCellToTechnician.type == CellType.AIR) {
                    return new MoveCommand(shortestCellToTechnician.x, shortestCellToTechnician.y);
                }else if (shortestCellToTechnician.type == CellType.DIRT) {
                    return new DigCommand(shortestCellToTechnician.x, shortestCellToTechnician.y);
                }
            }

            //Apabila Commander dan Technician sudah berdekatan, maka Commander akan bergerak mencari musuh terdekat untuk melancarkan serangan
            int min = 10000000;
            Worm wormMusuhTerdekat = opponent.worms[0];
            for (Worm calonMusuh : opponent.worms) {
                if (euclideanDistance(currentWorm.position.x, currentWorm.position.y, calonMusuh.position.x, calonMusuh.position.y) < min) {
                    min = euclideanDistance(currentWorm.position.x, currentWorm.position.y, calonMusuh.position.x, calonMusuh.position.y);
                    wormMusuhTerdekat = calonMusuh;
                }
            }

            //Mencari cell yang paling dekat ke musuh yang sudah ditemukan
            Cell shortestCellToEnemy = getShortestPath(surroundingBlocks, wormMusuhTerdekat.position.x, wormMusuhTerdekat.position.y);
            if(shortestCellToEnemy.type == CellType.AIR) {
                return new MoveCommand(shortestCellToEnemy.x, shortestCellToEnemy.y);
            }else if (shortestCellToEnemy.type == CellType.DIRT) {
                return new DigCommand(shortestCellToEnemy.x, shortestCellToEnemy.y);
            }
        }

        //Command move untuk worm selain Commando. Worm selain commando akan mendekat menuju posisi worm Commando
        Worm commandoWorm = gameState.myPlayer.worms[0];
        
        //Selama Commando masih hidup, maka Worm lainnya akan mendekat menuju Commando
        if (commandoWorm.health > 0) {
            //Cell cellCommandoWorm = surroundingBlocks.get(0);

            //Mencari cell yang membuat jarak menuju Commando paling dekat
            Cell shortestCellToCommander = getShortestPath(surroundingBlocks, commandoWorm.position.x, commandoWorm.position.y);

            if(shortestCellToCommander.type == CellType.AIR) {
                return new MoveCommand(shortestCellToCommander.x, shortestCellToCommander.y);
            }else if (shortestCellToCommander.type == CellType.DIRT) {
                return new DigCommand(shortestCellToCommander.x, shortestCellToCommander.y);
            }
        }

        // PRIORITAS 4: Bergerak secara acak untuk memancing musuh mendekat
        int cellIdx = random.nextInt(surroundingBlocks.size());
        Cell block = surroundingBlocks.get(cellIdx);
        if (block.type == CellType.AIR) {
            return new MoveCommand(block.x, block.y);
        } else if (block.type == CellType.DIRT) {
            return new DigCommand(block.x, block.y);
        }
        // Kalau udah enggak bisa ngapa-ngapain.
        return new DoNothingCommand();
        
    }

    //Untuk mencari Worm musuh yang masuk ke dalam jangkauan Normal Attack (Shoot). Apabila tidak ditemukan, return Null
    private Worm getFirstWormInRange() {

        Set<String> cells = constructFireDirectionLines(currentWorm.weapon.range)
                .stream()
                .flatMap(Collection::stream)
                .map(cell -> String.format("%d_%d", cell.x, cell.y))
                .collect(Collectors.toSet());

        for (Worm enemyWorm : opponent.worms) {
            String enemyPosition = String.format("%d_%d", enemyWorm.position.x, enemyWorm.position.y);
            if (cells.contains(enemyPosition) && enemyWorm.health > 0) {
                return enemyWorm;
            }
        }

        return null;
    }

    //Mencari Worm musuh yang masuk ke dalam jangkauan Special Attack (Banana Bomb/Snowball)
    private Worm getFirstWormInRangeSpecial() {
        
        int count = 0;
        Worm targetWorm = null;

        //Jika worm saat ini adalah Agent (bisa menggunakan Banana Bomb)
        if (gameState.myPlayer.worms[1].id == gameState.currentWormId) {
            count = gameState.myPlayer.worms[1].bananaBombs.count;
            int range = gameState.myPlayer.worms[1].bananaBombs.range;
            for (Worm enemyWorm : opponent.worms) {

                if (euclideanDistance(currentWorm.position.x, currentWorm.position.y, enemyWorm.position.x, enemyWorm.position.y) > range) {
                    continue;
                }
                if (enemyWorm.health <= 0) {
                    continue;
                }
                //AUTO NYERANG WORM YANG PERTAMAKALI DITEMUKAN DI DALAM RANGE
                targetWorm = enemyWorm;
                break;
            }

        }

        //Jika worm saat ini adalah Technician (bisa menggunakan Snowball)
        if (gameState.myPlayer.worms[2].id == gameState.currentWormId) {
            count = gameState.myPlayer.worms[2].snowballs.count;
            int range = gameState.myPlayer.worms[2].snowballs.range;
            for (Worm enemyWorm : opponent.worms) {

                if (euclideanDistance(currentWorm.position.x, currentWorm.position.y, enemyWorm.position.x, enemyWorm.position.y) > range) {
                    continue;
                }
                if (enemyWorm.roundsUntilUnfrozen > 1) {
                    continue;
                }
                //AUTO NYERANG WORM YANG PERTAMAKALI DITEMUKAN DI DALAM RANGE
                targetWorm = enemyWorm;
                break;
            }

        }
        
        return targetWorm;

    }


    //Membuat opsi Normal Attack (Shoot) yang mungkin
    private List<List<Cell>> constructFireDirectionLines(int range) {
        List<List<Cell>> directionLines = new ArrayList<>();
        for (Direction direction : Direction.values()) {
            List<Cell> directionLine = new ArrayList<>();
            for (int directionMultiplier = 1; directionMultiplier <= range; directionMultiplier++) {

                int coordinateX = currentWorm.position.x + (directionMultiplier * direction.x);
                int coordinateY = currentWorm.position.y + (directionMultiplier * direction.y);

                if (!isValidCoordinate(coordinateX, coordinateY)) {
                    break;
                }

                if (euclideanDistance(currentWorm.position.x, currentWorm.position.y, coordinateX, coordinateY) > range) {
                    break;
                }

                Cell cell = gameState.map[coordinateY][coordinateX];
                if (cell.type != CellType.AIR) {
                    break;
                }

                directionLine.add(cell);
            }
            directionLines.add(directionLine);
        }

        return directionLines;
    }

    //Mendapatkan alamat Cell seluruh Cell di sekitar Worm saat ini
    private List<Cell> getSurroundingCells(int x, int y) {
        ArrayList<Cell> cells = new ArrayList<>();
        for (int i = x - 1; i <= x + 1; i++) {
            for (int j = y - 1; j <= y + 1; j++) {
                // Don't include the current position i != x && j != y &&
                if ( isValidCoordinate(i, j)) {
                    if (i == x && j == y) {
                        continue;
                    } else {
                    cells.add(gameState.map[j][i]);
                    }
                }
            }
        }
        return cells;
    }

    //Method menghitung jarak
    private int euclideanDistance(int aX, int aY, int bX, int bY) {
        return (int) (Math.sqrt(Math.pow(aX - bX, 2) + Math.pow(aY - bY, 2)));
    }

    //Method mengecek ke-valid-an Cell
    private boolean isValidCoordinate(int x, int y) {
        return x >= 0 && x < gameState.mapSize
                && y >= 0 && y < gameState.mapSize;
    }

    private Direction resolveDirection(Position a, Position b) {
        StringBuilder builder = new StringBuilder();

        int verticalComponent = b.y - a.y;
        int horizontalComponent = b.x - a.x;

        if (verticalComponent < 0) {
            builder.append('N');
        } else if (verticalComponent > 0) {
            builder.append('S');
        }

        if (horizontalComponent < 0) {
            builder.append('W');
        } else if (horizontalComponent > 0) {
            builder.append('E');
        }

        return Direction.valueOf(builder.toString());
    }

    //Method untuk mendapatkan Cell terdekat ke tujuan
    private Cell getShortestPath(List<Cell> surroundingBlocks, int DestinationX, int DestinationY) {
        
        int cellIdx = random.nextInt(surroundingBlocks.size());
        Cell min = surroundingBlocks.get(0);
        int distMin = euclideanDistance(surroundingBlocks.get(0).x, surroundingBlocks.get(0).y, DestinationX, DestinationY);
        
        for(int i = 0; i < surroundingBlocks.size(); i++) {
            if (i==1) {
                min = surroundingBlocks.get(i);
            }
            if(distMin > euclideanDistance(surroundingBlocks.get(i).x, surroundingBlocks.get(i).y, DestinationX, DestinationY)) {
                distMin = euclideanDistance(surroundingBlocks.get(i).x, surroundingBlocks.get(i).y, DestinationX, DestinationY);
                min = surroundingBlocks.get(i);
            }
        }
        return min;
    }


}
