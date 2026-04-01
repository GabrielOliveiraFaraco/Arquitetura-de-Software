interface ISkateShop
{
    void SellSkateboard(string model);
    void CreateCustomSkateboard(string model, string shape, string wheels, string deck, string trucks, string gripTape, string bearings);
    void ReadSkateboardDetails(string model);
    void UpdateSkateboardDetails(string model, string shape, string wheels, string deck, string trucks, string gripTape, string bearings);
    void DeleteSkateboard(string model);
}

class SkateShop : ISkateShop
{
    public void SellSkateboard(string model)
    {
        Console.WriteLine($"Selling skateboard model: {model}");
    }

    public void CreateCustomSkateboard(string model, string shape, string wheels, string deck, string trucks, string gripTape, string bearings)
    {
        Console.WriteLine($"Creating custom skateboard model: {model} with shape: {shape}, wheels: {wheels}, deck: {deck}, trucks: {trucks}, grip tape: {gripTape}, bearings: {bearings}");
    }

    public void ReadSkateboardDetails(string model)
    {
        Console.WriteLine($"Reading details for skateboard model: {model}");
    }

    public void UpdateSkateboardDetails(string model, string shape, string wheels, string deck, string trucks, string gripTape, string bearings)
    {
        Console.WriteLine($"Updating skateboard model: {model} with new details - shape: {shape}, wheels: {wheels}, deck: {deck}, trucks: {trucks}, grip tape: {gripTape}, bearings: {bearings}");
    }

    public void DeleteSkateboard(string model)
    {
        Console.WriteLine($"Deleting skateboard model: {model}");
    }
}

class Program
{
    static void Main(string[] args)
    {
        ISkateShop skateShop = new SkateShop();

        skateShop.CreateCustomSkateboard("JapanBoard","LightWood", "Flip", "Element", "Crab", "Vinho Hardware", "Red Bones");
        skateShop.ReadSkateboardDetails("JapanBoard");
        skateShop.UpdateSkateboardDetails("JapanBoard", "Cruiser", "Ricta", "Santa Cruz", "Thunder", "Jessup", "Bronson");
        skateShop.SellSkateboard("JapanBoard");
        skateShop.DeleteSkateboard("JapanBoard");
    }
}
