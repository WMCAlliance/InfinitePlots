/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package uk.co.jacekk.bukkit.infiniteplots.generation;

import java.util.List;
import org.bukkit.block.Biome;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.WorldInfo;

/**
 *
 * @author WizardCM
 */
public class PlainsBiomeProvider extends BiomeProvider {

  @Override
  public Biome getBiome(WorldInfo worldInfo, int x, int y, int z) {
    return Biome.PLAINS;
  }

  @Override
  public List<Biome> getBiomes(WorldInfo worldInfo) {
    return List.of(Biome.PLAINS);
  }

}