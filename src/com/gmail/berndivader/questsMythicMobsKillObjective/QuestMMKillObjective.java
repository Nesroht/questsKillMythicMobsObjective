package com.gmail.berndivader.questsMythicMobsKillObjective;

import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

import me.blackvein.quests.CustomObjective;
import me.blackvein.quests.Quest;
import me.blackvein.quests.Quester;
import me.blackvein.quests.Quests;

public class QuestMMKillObjective extends CustomObjective implements Listener {

	private String strMMVer;
	private int mmVer;
	
	public QuestMMKillObjective() {
		setName("Kill MythicMobs Objective");
		setAuthor("BerndiVader");
		addData("Objective Name");
		addDescription("Objective Name", "Name your objective");
		addData("Internal Mobnames");
		addDescription("Internal Mobnames", "List of MythicMobs Types to use. Split with <,> or use ANY for any MythicMobs mobs.");
		addData("Mob Level");
		addDescription("Mob Level", "Level to match. 0 for every level, any singlevalue, or rangedvalue. Example: 2-5");
		addData("Mob Faction");
		addDescription("Mob Faction", "Faction of the mob to match. Split with <,> or use ANY for any mob faction");
		setEnableCount(true);
		setShowCount(true);
		setCountPrompt("How many MythicMobs to kill");
		setDisplay("%Objective Name%, Counter: %count%");
    	strMMVer = Bukkit.getServer().getPluginManager().getPlugin("MythicMobs").getDescription().getVersion().replaceAll("[\\D]", "");
		mmVer = Integer.valueOf(strMMVer);
	}

	public int getCounter() {
		return this.getCount();
	}
	
	@EventHandler
	public void onMythicMobDeathEvent (EntityDeathEvent e) {
		if (!(e.getEntity().getKiller() instanceof Player)) return;
		String mobtype = null;
		String f = null;
		int moblevel = 0;
		Player p = e.getEntity().getKiller();
		Entity bukkitentity = e.getEntity();
		if ((mmVer > 244 && mmVer < 252 || mmVer == 2511)) {
			net.elseland.xikage.MythicMobs.Mobs.ActiveMob am = 
					net.elseland.xikage.MythicMobs.MythicMobs.inst().getAPI().getMobAPI().getMythicMobInstance(bukkitentity);
			if (am==null) return;
			mobtype = am.getType().getInternalName();
			moblevel = am.getLevel();
			if (am.hasFaction()) f = am.getFaction();
		} else if (mmVer > 259 && mmVer < 2511) {
			io.lumine.xikage.mythicmobs.mobs.ActiveMob am = 
					io.lumine.xikage.mythicmobs.MythicMobs.inst().getMobManager().getMythicMobInstance(bukkitentity);
			if (am==null) return;
			mobtype = am.getType().getInternalName();
			moblevel = am.getLevel();
			if (am.hasFaction()) f = am.getFaction();
		}
		if (mobtype == null || mobtype.isEmpty()) return;
		Quester qp = Quests.getInstance().getQuester(p.getUniqueId());
		if (qp.currentQuests.isEmpty()) return;
		for (Quest q : qp.currentQuests.keySet()) {
			Map<?, ?> m = QuestMMKillObjective.getDatamap(p, this, q);
			if (m == null) continue;
			Object maybeKT = m.get("Internal Mobnames");
			Object maybePARSE = m.get("Mob Level");
			Object maybeFaction = m.get("Mob Faction");
			String[] kt = null;
			String[] parseLvl = null;
			String[] faction = null;
			if (maybeKT!=null && maybeKT instanceof String) {
				kt = m.get("Internal Mobnames").toString().split(",");
			} else {
				kt = new String[]{"ANY"};
			}
			if (maybePARSE!=null && maybePARSE instanceof String) {
				parseLvl = m.get("Mob Level").toString().split("-");
			} else {
				parseLvl = new String[]{"0"};
			}
			int level = 0; int lmin = 0;int lmax=0;
			if (parseLvl.length==1) {
				level = 1; 
				lmin = Integer.valueOf(parseLvl[0]);
				if (lmin==0) level = 0;
			} else if (parseLvl.length==2) {
				level = 2;
				lmin = Integer.valueOf(parseLvl[0]);
				lmax = Integer.valueOf(parseLvl[1]);
				if (lmin>lmax) level = 0;
			}
			if (maybeFaction!=null && maybeFaction instanceof String) {
				faction = m.get("Mob Faction").toString().split(",");
			} else {
				faction = new String[]{"ANY"};
			}
			if ((level==0) || (level==1 && moblevel==lmin) || (level==2 && (lmin<=moblevel && lmax>=moblevel))) {
				if (kt[0].equals("ANY") || ArrayUtils.contains(kt, mobtype)) {
					if (faction[0].equals("ANY") || ArrayUtils.contains(faction, f)) {
						QuestMMKillObjective.incrementObjective(p, this, 1, q);
					}
				}
			}
		}
	}
}