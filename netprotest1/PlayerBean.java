package netprotest1;

import java.io.Serializable;
import java.nio.channels.SocketChannel;

public class PlayerBean implements Serializable{
    private float x,y;
    private int bulletType;
    private boolean bulletBool;
    private float hp;
    private int playerNumber;

    /**
     * @return the x
     */
    public float getX() {
        return x;
    }

    /**
     * @param x the x to set
     */
    public void setX(float x) {
        this.x = x;
    }

    /**
     * @return the y
     */
    public float getY() {
        return y;
    }

    /**
     * @param y the y to set
     */
    public void setY(float y) {
        this.y = y;
    }

    /**
     * @return the bulletType
     */
    public int getBulletType() {
        return bulletType;
    }

    /**
     * @param bulletType the bulletType to set
     */
    public void setBulletType(int bulletType) {
        this.bulletType = bulletType;
    }

    /**
     * @return the bulletBool
     */
    public boolean isBulletBool() {
        return bulletBool;
    }

    /**
     * @param bulletBool the bulletBool to set
     */
    public void setBulletBool(boolean bulletBool) {
        this.bulletBool = bulletBool;
    }

    /**
     * @return the hp
     */
    public float getHp() {
        return hp;
    }

    /**
     * @param hp the hp to set
     */
    public void setHp(float hp) {
        this.hp = hp;
    }

    /**
     * @return the playerNumber
     */
    public int getPlayerNumber() {
        return playerNumber;
    }

    /**
     * @param playerNumber the playerNumber to set
     */
    public void setPlayerNumber(int playerNumber) {
        this.playerNumber = playerNumber;
    }
}