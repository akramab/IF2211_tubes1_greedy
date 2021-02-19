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
        //Special Attack dulu kalau bisa
        Worm enemyWorm = getFirstWormInRangeSpecial();
        if (enemyWorm != null) {
            if (gameState.myPlayer.worms[1].id == gameState.currentWormId) {
                if (gameState.myPlayer.worms[1].bananaBombs.count > 0) {
                    return new BananaBombCommand(enemyWorm.position.x, enemyWorm.position.y);
                }
            }
            if (gameState.myPlayer.worms[2].snowballs.count > 0) {
                return new SnowballCommand(enemyWorm.position.x, enemyWorm.position.y);
            }
        }

        System.out.println(gameState.myPlayer.worms[1].bananaBombs);
        //Normal Attack kalau bisa
        enemyWorm = getFirstWormInRange();
        if (enemyWorm != null) {
            Direction direction = resolveDirection(currentWorm.position, enemyWorm.position);
            return new ShootCommand(direction);
        }

        List<Cell> surroundingBlocks = getSurroundingCells(currentWorm.position.x, currentWorm.position.y);
        

        //BUAT DEBUGGING
        for(int i = 0; i < surroundingBlocks.size();i++){
            System.out.print(surroundingBlocks.get(i).x);
            System.out.print(" ");
            System.out.println(surroundingBlocks.get(i).y);
        }

        
        if(gameState.currentWormId == 1){

            Worm agentWorm = gameState.myPlayer.worms[2];
            Cell shortestCellToAgent = getShortestPath(surroundingBlocks, agentWorm.position.x, agentWorm.position.y);
             //BUAT DEBUGGING
            System.out.println("BAGIAN COMMANDER FOLLOW AGENT");
            for(int i = 0; i < surroundingBlocks.size();i++){
                System.out.print(surroundingBlocks.get(i).x);
                System.out.print(" ");
                System.out.println(surroundingBlocks.get(i).y);
            }
            //---------------------
            if(shortestCellToAgent.type == CellType.AIR) {
                return new MoveCommand(shortestCellToAgent.x, shortestCellToAgent.y);
            }else if (shortestCellToAgent.type == CellType.DIRT) {
                return new DigCommand(shortestCellToAgent.x, shortestCellToAgent.y);
            }
//             if (block.type == CellType.AIR) {
//                 return new MoveCommand(block.x, block.y);
//             } else if (block.type == CellType.DIRT) {
//                 return new DigCommand(block.x, block.y);
//             }
        }

        //followCommand
        Worm commandoWorm = gameState.myPlayer.worms[0];
        //int commandoPositionY = gameState.worms[0].position.y;
        if (commandoWorm.health > 0) {
            Cell cellCommandoWorm = surroundingBlocks.get(0);

            //getShortestPath
            Cell shortestCellToCommander = getShortestPath(surroundingBlocks, commandoWorm.position.x, commandoWorm.position.y);

             //BUAT DEBUGGING
            System.out.println("BAGIAN FOLLOW COMMANDER");
            for(int i = 0; i < surroundingBlocks.size();i++){
                System.out.print(surroundingBlocks.get(i).x);
                System.out.print(" ");
                System.out.println(surroundingBlocks.get(i).y);
            }

            if(shortestCellToCommander.type == CellType.AIR) {
                return new MoveCommand(shortestCellToCommander.x, shortestCellToCommander.y);
            }else if (shortestCellToCommander.type == CellType.DIRT) {
                return new DigCommand(shortestCellToCommander.x, shortestCellToCommander.y);
            }
            //BUAT DEBUGGING
            System.out.println("BAGIAN SETELAH SHORTESTPATH");
            for(int i = 0; i < surroundingBlocks.size();i++){
                System.out.print(surroundingBlocks.get(i).x);
                System.out.print(" ");
                System.out.println(surroundingBlocks.get(i).y);
            }
            
        }

        // MOVE RANDOMLY IF POSSIBLE
        int cellIdx = random.nextInt(surroundingBlocks.size());
        Cell block = surroundingBlocks.get(cellIdx);
        if (block.type == CellType.AIR) {
            return new MoveCommand(block.x, block.y);
        } else if (block.type == CellType.DIRT) {
            return new DigCommand(block.x, block.y);
        }
        // IF NOTHING ELSE
        return new DoNothingCommand();
        
    }

    private Worm getFirstWormInRange() {

        Set<String> cells = constructFireDirectionLines(currentWorm.weapon.range)
                .stream()
                .flatMap(Collection::stream)
                .map(cell -> String.format("%d_%d", cell.x, cell.y))
                .collect(Collectors.toSet());

        for (Worm enemyWorm : opponent.worms) {
            String enemyPosition = String.format("%d_%d", enemyWorm.position.x, enemyWorm.position.y);
            if (cells.contains(enemyPosition)) {
                return enemyWorm;
            }
        }

        return null;
    }

    private Worm getFirstWormInRangeSpecial() {
        
        int count = 0;
        Worm targetWorm = null;
        if (gameState.myPlayer.worms[1].id == gameState.currentWormId) {
            count = gameState.myPlayer.worms[1].bananaBombs.count;
            int range = gameState.myPlayer.worms[1].bananaBombs.range;
            for (Worm enemyWorm : opponent.worms) {

                if (euclideanDistance(currentWorm.position.x, currentWorm.position.y, enemyWorm.position.x, enemyWorm.position.y) > range) {
                    continue;
                }
                //AUTO NYERANG WORM YANG PERTAMAKALI DITEMUKAN DI DALAM RANGE
                targetWorm = enemyWorm;
                break;
            }

        }
        if (gameState.myPlayer.worms[2].id == gameState.currentWormId) {
            count = gameState.myPlayer.worms[2].snowballs.count;
            int range = gameState.myPlayer.worms[2].snowballs.range;
            for (Worm enemyWorm : opponent.worms) {

                if (euclideanDistance(currentWorm.position.x, currentWorm.position.y, enemyWorm.position.x, enemyWorm.position.y) > range) {
                    continue;
                }
                //AUTO NYERANG WORM YANG PERTAMAKALI DITEMUKAN DI DALAM RANGE
                targetWorm = enemyWorm;
                break;
            }

        }
        // if(count == 0) {
        //     return null;
        // }
        return targetWorm;



        // for (Worm enemyWorm : opponent.worms) {
        //     String enemyPosition = String.format("%d_%d", enemyWorm.position.x, enemyWorm.position.y);
        //     if (cells.contains(enemyPosition)) {
        //         return enemyWorm;
        //     }
        // }

        // return null;
    }

    //TESTING
    private List<List<Cell>> constructSpecialDirectionLines(int range) {
        List<List<Cell>> directionLines = new ArrayList<>();
        List<Cell> directionLine = new ArrayList<>();
        for (Worm enemyWorm : opponent.worms) {

        if (euclideanDistance(currentWorm.position.x, currentWorm.position.y, enemyWorm.position.x, enemyWorm.position.y) > range) {
                continue;
            }
            Cell cell = gameState.map[enemyWorm.position.x][enemyWorm.position.y];
            directionLine.add(cell);
        }
        if (directionLine.size() != 0){
            directionLines.add(directionLine);
            return directionLines;
        }
        return null;

    }
    //END OF TESTING

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
        System.out.println("KOORDINAT SEKARANG:");
        System.out.print(x);
        System.out.print(" ");
        System.out.println(y);

        return cells;
    }

    private int euclideanDistance(int aX, int aY, int bX, int bY) {
        return (int) (Math.sqrt(Math.pow(aX - bX, 2) + Math.pow(aY - bY, 2)));
    }

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

    // private Command followLeader(List<Cell> surroundingBlocks) {
    //     Worm commandoWorm = gameState.myPlayer.worms[0];
    //     //int commandoPositionY = gameState.worms[0].position.y;
    //     if (currentWorm.position.x != commandoWorm.position.x && currentWorm.position.y != commandoWorm.position.y) {
    //         // List<Cell> surroundingBlocks = getSurroundingCells(currentWorm.position.x, currentWorm.position.y);
    //         Cell cellCommandoWorm = surroundingBlocks.get(0);
    //         cellCommandoWorm.x = commandoWorm.position.x;
    //         cellCommandoWorm.y = commandoWorm.position.y;
    //         Cell shortestCellToCommander = getShortestPath(surroundingBlocks, cellCommandoWorm);

    //         if(shortestCellToCommander.type == CellType.AIR) {
    //             return new MoveCommand(shortestCellToCommander.x, shortestCellToCommander.y);
    //         }else if (shortestCellToCommander.type == CellType.DIRT) {
    //             return new DigCommand(shortestCellToCommander.x, shortestCellToCommander.y);
    //         }
            
    //     }
    //     return new DoNothingCommand();   
    // }

    private Cell getShortestPath(List<Cell> surroundingBlocks, int DestinationX, int DestinationY) {
        // List<Cell> surroundingBlocks = getSurroundingCells(currentWorm.position.x, currentWorm.position.y);
        int cellIdx = random.nextInt(surroundingBlocks.size());
        Cell min = surroundingBlocks.get(0);
        // min.x =surroundingBlocks.get().x;
        // min.y =surroundingBlocks.get(0).y;
        int distMin = euclideanDistance(surroundingBlocks.get(0).x, surroundingBlocks.get(0).y, DestinationX, DestinationY);
        System.out.println("Available Blocks");
        for(int i = 0; i < surroundingBlocks.size(); i++) {
            if (i==1) {
                min = surroundingBlocks.get(i);
            }
            System.out.print(surroundingBlocks.get(i).x);
            System.out.print(" ");
            System.out.println(surroundingBlocks.get(i).y);
            if(distMin > euclideanDistance(surroundingBlocks.get(i).x, surroundingBlocks.get(i).y, DestinationX, DestinationY)) {
                distMin = euclideanDistance(surroundingBlocks.get(i).x, surroundingBlocks.get(i).y, DestinationX, DestinationY);
                min = surroundingBlocks.get(i);
            }
        }
        System.out.println("==============");
        System.out.println(surroundingBlocks.get(0).x);
        System.out.print(min.x);
        System.out.print(" ");
        System.out.println(min.y);
        return min;
    }


}