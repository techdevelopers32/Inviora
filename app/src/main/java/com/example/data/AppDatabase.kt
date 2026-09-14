package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.AnimationDao
import com.example.data.dao.DesignDao
import com.example.data.dao.EventDao
import com.example.data.dao.GuestDao
import com.example.data.dao.PreferenceDao
import com.example.data.model.AnimationExperienceEntity
import com.example.data.model.AppPreferenceEntity
import com.example.data.model.DesignTemplateEntity
import com.example.data.model.EventEntity
import com.example.data.model.GuestEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
  entities = [
    DesignTemplateEntity::class,
    AnimationExperienceEntity::class,
    EventEntity::class,
    GuestEntity::class,
    AppPreferenceEntity::class
  ],
  version = 5,
  exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
  abstract fun designDao(): DesignDao
  abstract fun animationDao(): AnimationDao
  abstract fun eventDao(): EventDao
  abstract fun guestDao(): GuestDao
  abstract fun preferenceDao(): PreferenceDao

  companion object {
    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          AppDatabase::class.java,
          "inviora_database"
        )
          .addCallback(DatabaseCallback(scope))
          .fallbackToDestructiveMigration()
          .build()
        INSTANCE = instance
        instance
      }
    }

    private class DatabaseCallback(
      private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
      override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        INSTANCE?.let { database ->
          scope.launch(Dispatchers.IO) {
            populateInitialData(database)
          }
        }
      }

      override fun onOpen(db: SupportSQLiteDatabase) {
        super.onOpen(db)
        INSTANCE?.let { database ->
          scope.launch(Dispatchers.IO) {
            try {
              if (database.animationDao().getAnimationCount() < 25) {
                populateInitialData(database)
              }
            } catch (_: Exception) {}
          }
        }
      }

      private suspend fun populateInitialData(db: AppDatabase) {
        // Initial flagships created strictly according to the product principles
        val design1 = DesignTemplateEntity(
          id = "design_royal_gold",
          name = "Royal Gold Heritage",
          referenceDrawable = "ref_royal_card",
          themeStyle = "ROYAL_GOLD",
          accentColorHex = "#D4AF37",
          fontStyle = "Serif Calligraphic",
          ornamentStyle = "Baroque Gold Filigree",
          prompt = "Royal gold leaf borders with deep emerald and rich parchment background"
        )
        val design2 = DesignTemplateEntity(
          id = "design_botanical_cream",
          name = "Botanical Cream Atelier",
          referenceDrawable = "ref_botanical_card",
          themeStyle = "BOTANICAL_CREAM",
          accentColorHex = "#8F9779",
          fontStyle = "Editorial Serif",
          ornamentStyle = "Eucalyptus & Gold Speckles",
          prompt = "Deckled cotton paper with hand-painted watercolor botanical wreath"
        )
        val design3 = DesignTemplateEntity(
          id = "design_noir_luxe",
          name = "Obsidian Noir Luxe",
          referenceDrawable = "ref_palace_doors",
          themeStyle = "NOIR_VELVET",
          accentColorHex = "#E5C07B",
          fontStyle = "Modern Luxury",
          ornamentStyle = "Minimalist Art Deco Geometry",
          prompt = "Deep obsidian velvet texture with geometric champaign foil typography"
        )
        db.designDao().insertDesign(design1)
        db.designDao().insertDesign(design2)
        db.designDao().insertDesign(design3)

        // 20 Flagship Premium Cinematic Animation Experiences
        val all20Animations = listOf(
          AnimationExperienceEntity(
            id = "anim_velvet_curtain",
            name = "Imperial Velvet Curtain",
            referenceDrawable = "ref_velvet_curtain",
            animationType = "CURTAIN",
            motionTimingMs = 2600L,
            lightingMood = "Grand Spotlight",
            description = "Heavy crimson velvet fabric parting from center with realistic drapery folds and golden tassels"
          ),
          AnimationExperienceEntity(
            id = "anim_palace_doors",
            name = "Grand Palace Doors",
            referenceDrawable = "ref_palace_doors",
            animationType = "PALACE_DOORS",
            motionTimingMs = 2800L,
            lightingMood = "Warm Golden Sanctuary",
            description = "Monumental dark ebony doors with ornate gold carving swinging open in full 3D perspective"
          ),
          AnimationExperienceEntity(
            id = "anim_wax_envelope",
            name = "Royal Wax Seal Envelope",
            referenceDrawable = "ref_wax_envelope",
            animationType = "WAX_SEAL",
            motionTimingMs = 2500L,
            lightingMood = "Soft Atelier Glow",
            description = "Handmade textured envelope with ruby wax seal breaking open and card emerging upward"
          ),
          AnimationExperienceEntity(
            id = "anim_champagne_silk",
            name = "Champagne Silk Drape",
            referenceDrawable = "ref_silk_drape",
            animationType = "SILK_DRAPE",
            motionTimingMs = 2400L,
            lightingMood = "Warm Amber Glow",
            description = "Flowing shimmering champagne silk lifting upward with graceful fluid ripples"
          ),
          AnimationExperienceEntity(
            id = "anim_gilded_arch",
            name = "Gilded Cathedral Archways",
            referenceDrawable = "ref_gilded_arch",
            animationType = "GILDED_ARCH",
            motionTimingMs = 2700L,
            lightingMood = "Sacred Golden Sunlight",
            description = "Intricate cathedral wrought-gold gates parting inward into luminous warmth"
          ),
          AnimationExperienceEntity(
            id = "anim_botanical_garland",
            name = "Botanical Leaf Garland",
            referenceDrawable = "ref_botanical_card",
            animationType = "BOTANICAL_GARLAND",
            motionTimingMs = 2300L,
            lightingMood = "Fresh Morning Mist",
            description = "Lush eucalyptus, olive leaves and golden sprigs gracefully unfurling to reveal the invitation"
          ),
          AnimationExperienceEntity(
            id = "anim_celestial_starlight",
            name = "Celestial Starlight Luminescence",
            referenceDrawable = "ref_celestial_stars",
            animationType = "STARLIGHT_GLOW",
            motionTimingMs = 2500L,
            lightingMood = "Midnight Cosmos",
            description = "Soft constellation of shimmering golden starlight coalescing and revealing the card canvas"
          ),
          AnimationExperienceEntity(
            id = "anim_folding_triptych",
            name = "Regal Folding Triptych",
            referenceDrawable = "ref_folding_triptych",
            animationType = "FOLDING_TRIPTYCH",
            motionTimingMs = 2600L,
            lightingMood = "Regal Warmth",
            description = "Three-panel luxury textured gatefold invitation unfolding outward with fine embossed crease physics"
          ),
          AnimationExperienceEntity(
            id = "anim_noir_gold_crest",
            name = "Obsidian Noir Gold Crest",
            referenceDrawable = "ref_gold_crest",
            animationType = "GOLD_CREST",
            motionTimingMs = 2400L,
            lightingMood = "Dramatic Monolith",
            description = "Deep obsidian velvet canvas with a gleaming hot-foil gold seal bursting into ambient light"
          ),
          AnimationExperienceEntity(
            id = "anim_royal_frame",
            name = "Baroque Gilt Mirror Frame",
            referenceDrawable = "ref_royal_frame",
            animationType = "BAROQUE_FRAME",
            motionTimingMs = 2500L,
            lightingMood = "Gilded Ballroom",
            description = "Hand-carved baroque filigree frame glowing as frosted center glass dissolves into clarity"
          ),
          AnimationExperienceEntity(
            id = "anim_paper_unfold",
            name = "Handcrafted Deckled Paper",
            referenceDrawable = "ref_deckled_paper",
            animationType = "PAPER_UNFOLD",
            motionTimingMs = 2200L,
            lightingMood = "Artisan Daylight",
            description = "Handmade raw cotton paper with feathered deckled edges floating down with organic tactile realism"
          ),
          AnimationExperienceEntity(
            id = "anim_luxury_box",
            name = "Velvet Keepsake Box",
            referenceDrawable = "ref_luxury_box",
            animationType = "KEEPSAKE_BOX",
            motionTimingMs = 2700L,
            lightingMood = "Jeweled Presentation",
            description = "Lacquered keepsake presentation box with velvet lining whose lid lifts open to present the invitation"
          ),
          AnimationExperienceEntity(
            id = "anim_ribbon_tie",
            name = "Satin Ribbon Untie",
            referenceDrawable = "ref_ribbon_tie",
            animationType = "RIBBON_UNTIE",
            motionTimingMs = 2300L,
            lightingMood = "Chic Studio Glow",
            description = "Double-faced metallic gold satin ribbon bow gently coming untied and sliding off the envelope"
          ),
          AnimationExperienceEntity(
            id = "anim_soft_glow",
            name = "Morning Aurora Soft Glow",
            referenceDrawable = "ref_soft_glow",
            animationType = "SOFT_GLOW",
            motionTimingMs = 2100L,
            lightingMood = "Golden Hour Sunlight",
            description = "Diffused golden light beam sweeping diagonally across sheer linen fabric"
          ),
          AnimationExperienceEntity(
            id = "anim_monogram_stamp",
            name = "Vintage Monogram Stamp",
            referenceDrawable = "ref_monogram_stamp",
            animationType = "MONOGRAM_STAMP",
            motionTimingMs = 2400L,
            lightingMood = "Vintage Study",
            description = "Custom family monogram stamp descending onto hot red sealing wax and gleaming with gold leaf"
          ),
          AnimationExperienceEntity(
            id = "anim_emerald_velvet",
            name = "Deep Emerald Velvet Reveal",
            referenceDrawable = "ref_emerald_velvet",
            animationType = "EMERALD_VELVET",
            motionTimingMs = 2600L,
            lightingMood = "Royal Garden Night",
            description = "Forest emerald velvet drapes parting diagonally with weighted golden tassels"
          ),
          AnimationExperienceEntity(
            id = "anim_rose_gold_sparkle",
            name = "Rose Gold Shimmer",
            referenceDrawable = "ref_rose_gold",
            animationType = "ROSE_GOLD_SHIMMER",
            motionTimingMs = 2300L,
            lightingMood = "Blush Romance",
            description = "Delicate rose gold glitter dust dispersing in an airy breeze to reveal the invitation face"
          ),
          AnimationExperienceEntity(
            id = "anim_origami_bloom",
            name = "Geometric Origami Blossom",
            referenceDrawable = "ref_origami_bloom",
            animationType = "ORIGAMI_BLOOM",
            motionTimingMs = 2500L,
            lightingMood = "Modern Zen Luxury",
            description = "Architectural geometric paper petals unfolding sequentially in three dimensions"
          ),
          AnimationExperienceEntity(
            id = "anim_couture_veil",
            name = "Couture Atelier Veil",
            referenceDrawable = "ref_couture_veil",
            animationType = "COUTURE_VEIL",
            motionTimingMs = 2400L,
            lightingMood = "Haute Couture Softbox",
            description = "Translucent ethereal silk veil billowing softly and drifting away into the air"
          ),
          AnimationExperienceEntity(
            id = "anim_palace_fretwork",
            name = "Moroccan Palace Fretwork",
            referenceDrawable = "ref_palace_fretwork",
            animationType = "PALACE_FRETWORK",
            motionTimingMs = 2700L,
            lightingMood = "Alhambra Courtyard",
            description = "Geometric carved mashrabiya lattice shutters sliding open as warm lantern light floods through"
          ),
          AnimationExperienceEntity(
            id = "anim_golden_light",
            name = "Golden Hour Sunbeam",
            referenceDrawable = "ref_royal_card",
            animationType = "GOLDEN_LIGHT",
            motionTimingMs = 2400L,
            lightingMood = "Golden Hour Sunbeam",
            description = "Radiant diagonal amber sunlight shaft sweeping across the card and resolving into pure clarity"
          ),
          AnimationExperienceEntity(
            id = "anim_scroll_unroll",
            name = "Royal Parchment Scroll",
            referenceDrawable = "ref_royal_card",
            animationType = "SCROLL_UNROLL",
            motionTimingMs = 2700L,
            lightingMood = "Royal Treasury",
            description = "Heavy parchment scroll unrolling smoothly top and bottom with carved gold finials"
          ),
          AnimationExperienceEntity(
            id = "anim_double_doors",
            name = "Classic French Salon Doors",
            referenceDrawable = "ref_palace_doors",
            animationType = "DOUBLE_DOORS",
            motionTimingMs = 2600L,
            lightingMood = "Parisian Afternoon",
            description = "White lacquered double French doors with glass panes and brass hardware swinging outward"
          ),
          AnimationExperienceEntity(
            id = "anim_floral_bloom",
            name = "Peony & Jasmine Bloom",
            referenceDrawable = "ref_botanical_card",
            animationType = "FLORAL_BLOOM",
            motionTimingMs = 2500L,
            lightingMood = "Romantic Sunset Garden",
            description = "Lush romantic blush peonies and jasmine blossoms opening radially with petal rotation"
          ),
          AnimationExperienceEntity(
            id = "anim_crystal_glass",
            name = "Crystal Frosted Glass",
            referenceDrawable = "ref_palace_doors",
            animationType = "CRYSTAL_GLASS",
            motionTimingMs = 2400L,
            lightingMood = "Prismatic Glow",
            description = "Luxury beveled frosted glass pane with refractive rainbow sparkles melting into clarity"
          ),
          AnimationExperienceEntity(
            id = "anim_cinematic_shadow",
            name = "Chiaroscuro Window Shadow",
            referenceDrawable = "ref_botanical_card",
            animationType = "CINEMATIC_SHADOW",
            motionTimingMs = 2300L,
            lightingMood = "Architectural Daylight",
            description = "Atmospheric arched window shutter shadow drifting smoothly off the card face"
          ),
          AnimationExperienceEntity(
            id = "anim_card_emerge",
            name = "Pocket Envelope Card Slide",
            referenceDrawable = "ref_wax_envelope",
            animationType = "CARD_EMERGE",
            motionTimingMs = 2600L,
            lightingMood = "Luxury Stationery Suite",
            description = "Invitation card smoothly elevating out of a midnight blue gold-embossed envelope pocket"
          )
        )
        for (anim in all20Animations) {
          db.animationDao().insertAnimation(anim)
        }
      }
    }
  }
}
