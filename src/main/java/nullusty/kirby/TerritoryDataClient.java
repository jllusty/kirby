package nullusty.kirby;

import java.util.List;

public interface TerritoryDataClient {
    TerritoryData getTerritoryByIndex(String index);
    List<TerritoryData> getAllTerritories();
}
