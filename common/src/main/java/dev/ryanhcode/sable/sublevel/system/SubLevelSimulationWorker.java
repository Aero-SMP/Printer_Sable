package dev.ryanhcode.sable.sublevel.system;

import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.api.sublevel.ServerSubLevelContainer;
import dev.ryanhcode.sable.physics.config.PhysicsConfigData;

import java.util.concurrent.locks.LockSupport;

public class SubLevelSimulationWorker implements AutoCloseable{

    private final Thread thread;
    private volatile boolean running;
    private final String name;
    private final ServerSubLevelContainer container;

    public SubLevelSimulationWorker(final ServerSubLevelContainer container){
        this.container = container;
        this.name = "sable-simulation-worker-" + container.getLevel().dimension().location().toString();
        this.thread = new Thread(this::runLoop, this.name);
    }

    private PhysicsConfigData getConfig(){
        return this.container.physicsSystem().getConfig();
    }

    public void start(){
        this.running = true;
        this.thread.start();
        Sable.LOGGER.info("Started {}", this.name);
    }

    private void runLoop(){
        long nextTickTime = System.nanoTime();
        while(this.running){
            try{
                final PhysicsConfigData config = this.getConfig();

                this.tickSimulation(config);

                final double tickRate = Double.isFinite(config.simulationTicksPerSecond) ? Math.max(1.0, config.simulationTicksPerSecond) : 20.0;
                final long tickNanos = (long) (1_000_000_000.0 / tickRate);
                nextTickTime = Math.max(nextTickTime + tickNanos, System.nanoTime());

                this.sleepUntil(nextTickTime);
            }
            catch (final InterruptedException e){
                Thread.currentThread().interrupt();
                return;
            }
            catch (final Throwable t){
                Sable.LOGGER.error("Simulation worker crashed",t);
                this.running = false;
                return;
            }
        }
    }

    private void sleepUntil(final long targetNanos) throws InterruptedException{
        long sleepNanos = targetNanos - System.nanoTime();

        while (this.running && sleepNanos > 0L){

            LockSupport.parkNanos(sleepNanos);

            if(Thread.interrupted()){
                throw new InterruptedException();
            }

            sleepNanos = targetNanos - System.nanoTime();
        }
    }

    private void tickSimulation(PhysicsConfigData config) throws InterruptedException{

    }

    @Override
    public void close(){
        this.running = false;
        this.thread.interrupt();
        try{
            this.thread.join();
        } catch (final InterruptedException e){
            Thread.currentThread().interrupt();
        }
        Sable.LOGGER.info("Stopped {}", this.name);
    }
}
